package ru.webim.android.sdk.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import ru.webim.android.sdk.BuildConfig;
import ru.webim.android.sdk.FatalErrorHandler;
import ru.webim.android.sdk.FatalErrorHandler.FatalErrorType;
import ru.webim.android.sdk.Message;
import ru.webim.android.sdk.MessageStream;
import ru.webim.android.sdk.NotFatalErrorHandler;
import ru.webim.android.sdk.ProvidedAuthorizationTokenStateListener;
import ru.webim.android.sdk.Webim;
import ru.webim.android.sdk.WebimError;
import ru.webim.android.sdk.WebimSession;
import ru.webim.android.sdk.impl.backend.AuthData;
import ru.webim.android.sdk.impl.backend.DefaultCallback;
import ru.webim.android.sdk.impl.backend.DeltaCallback;
import ru.webim.android.sdk.impl.backend.DeltaRequestLoop;
import ru.webim.android.sdk.impl.backend.InternalErrorListener;
import ru.webim.android.sdk.impl.backend.SessionParamsListener;
import ru.webim.android.sdk.impl.backend.WebimActions;
import ru.webim.android.sdk.impl.backend.WebimClient;
import ru.webim.android.sdk.impl.backend.WebimClientBuilder;
import ru.webim.android.sdk.impl.backend.WebimInternalError;
import ru.webim.android.sdk.impl.backend.WebimInternalLog;
import ru.webim.android.sdk.impl.items.ChatItem;
import ru.webim.android.sdk.impl.items.DepartmentItem;
import ru.webim.android.sdk.impl.items.HistoryRevisionItem;
import ru.webim.android.sdk.impl.items.MessageItem;
import ru.webim.android.sdk.impl.items.OnlineStatusItem;
import ru.webim.android.sdk.impl.items.OperatorItem;
import ru.webim.android.sdk.impl.items.RatingItem;
import ru.webim.android.sdk.impl.items.SurveyItem;
import ru.webim.android.sdk.impl.items.UnreadByVisitorMessagesItem;
import ru.webim.android.sdk.impl.items.VisitSessionStateItem;
import ru.webim.android.sdk.impl.items.delta.DeltaFullUpdate;
import ru.webim.android.sdk.impl.items.delta.DeltaItem;
import ru.webim.android.sdk.impl.items.responses.HistoryBeforeResponse;
import ru.webim.android.sdk.impl.items.responses.HistorySinceResponse;
import ru.webim.android.sdk.impl.items.responses.LocationStatusResponse;

public class WebimSessionImpl implements WebimSession {
    private static final String GUID_SHARED_PREFS_NAME = BuildConfig.LIBRARY_PACKAGE_NAME + ".guid";
    private static final String PLATFORM = "android";
    private static final String PREFS_KEY_AUTH_TOKEN = "auth_token";
    private static final String PREFS_KEY_HISTORY_DB_NAME = "history_db_name";
    private static final String PREFS_KEY_HISTORY_DB_PASSWORD = "history_db_password";
    private static final String PREFS_KEY_HISTORY_ENDED = "history_ended";
    private static final String PREFS_KEY_HISTORY_MAJOR_VERSION = "history_major_version";
    private static final String PREFS_KEY_HISTORY_REVISION = "history_revision";
    private static final String PREFS_KEY_PAGE_ID = "page_id";
    private static final String PREFS_KEY_PREVIOUS_ACCOUNT = "previous_account";
    private static final String PREFS_KEY_READ_BEFORE_TIMESTAMP = "read_before_timestamp";
    private static final String PREFS_KEY_SESSION_ID = "session_id";
    private static final String PREFS_KEY_VISITOR = "visitor";
    private static final String PREFS_KEY_VISITOR_EXT = "visitor_ext";
    private static final String PREFS_KEY_NON_ENCRYPTED_PREFERENCES_REMOVED = "non_safety_preferences_removed";
    private static final String SHARED_PREFS_NAME = BuildConfig.LIBRARY_PACKAGE_NAME + ".visitor.";
    private static final String SHARED_PREFS_NAME_DEPRECATED = "com.webimapp.android.sdk.visitor.";
    private static final String TITLE = "Android Client";
    @NonNull
    private final AccessChecker accessChecker;
    @NonNull
    private final WebimClient client;
    @NonNull
    private final SessionDestroyer destroyer;
    @NonNull
    private final HistoryPoller historyPoller;
    @Nullable
    private final LocationStatusPoller locationStatusPoller;
    @NonNull
    private final MessageStreamImpl stream;
    private boolean clientStarted;

    private WebimSessionImpl(
        @NonNull AccessChecker accessChecker,
        @NonNull SessionDestroyer destroyer,
        @NonNull WebimClient client,
        @NonNull HistoryPoller historyPoller,
        @NonNull MessageStreamImpl stream,
        @Nullable LocationStatusPoller locationStatusPoller) {
        this.accessChecker = accessChecker;
        this.destroyer = destroyer;
        this.client = client;
        this.historyPoller = historyPoller;
        this.stream = stream;
        this.locationStatusPoller = locationStatusPoller;
    }

    private void checkAccess() {
        accessChecker.checkAccess();
    }

    @Override
    public void resume() {
        checkAccess();
        if (!clientStarted) {
            client.start();
            clientStarted = true;
        }
        client.resume();
        historyPoller.resume();
        if (locationStatusPoller != null) {
            locationStatusPoller.resume();
        }
    }

    @Override
    public void pause() {
        if (destroyer.isDestroyed()) {
            return;
        }
        checkAccess();
        client.pause();
        historyPoller.pause();
        if (locationStatusPoller != null) {
            locationStatusPoller.pause();
        }
    }

    @Override
    public void destroy() {
        if (destroyer.isDestroyed()) {
            return;
        }
        checkAccess();
        destroyer.destroy();
    }

    @Override
    public void destroyWithClearVisitorData() {
        if (destroyer.isDestroyed()) {
            return;
        }
        checkAccess();
        removePushToken(new TokenCallback() {
            @Override
            public void onSuccess() {
                destroyer.destroyAndClearVisitorData();
            }

            @Override
            public void onFailure(WebimError<TokenError> webimError) {
                destroyer.destroyAndClearVisitorData();
            }
        });
    }

    @NonNull
    @Override
    public MessageStream getStream() {
        return stream;
    }

    @Override
    public void changeLocation(@NonNull String location) {
        client.getDeltaRequestLoop().changeLocation(location);
    }

    @Override
    public void setPushToken(@NonNull String pushToken) {
        checkAccess();
        client.setPushToken(pushToken, null);
        // FIXME this method may be invoked before checkPushToken callback executed, so the push token will be overwritten
    }

    @Override
    public void removePushToken(@NonNull TokenCallback tokenCallback) {
        checkAccess();
        String emptyPushToken = "none";
        client.setPushToken(emptyPushToken, tokenCallback);
    }

    @NonNull
    public static WebimSessionImpl newInstance(
            @NonNull Context context,
            @Nullable SharedPreferences preferences,
            @NonNull String accountName,
            @NonNull String location,
            @Nullable String appVersion,
            @Nullable ProvidedVisitorFields visitorFields,
            @Nullable String prechatFields,
            @Nullable ProvidedAuthorizationTokenStateListener
                    providedAuthorizationTokenStateListener,
            @Nullable String providedAuthorizationToken,
            @Nullable String title,
            @Nullable FatalErrorHandler errorHandler,
            @Nullable NotFatalErrorHandler notFatalErrorHandler,
            @Nullable Webim.PushSystem pushSystem,
            @Nullable String pushToken,
            boolean storeHistoryLocally,
            boolean clearVisitorData,
            SSLSocketFactory sslSocketFactory,
            X509TrustManager trustManager,
            @NonNull String multivisitorSection,
            @Nullable SessionCallback sessionCallback,
            long requestLocationFrequency,
            @Nullable MessageParsingErrorHandler messageParsingErrorHandler
    ) {
        context.getClass(); // NPE
        accountName.getClass(); // NPE
        location.getClass(); // NPE

        if (Looper.myLooper() == null) {
            throw new RuntimeException("The Thread on which Webim session creates " +
                "should have attached android.os.Looper object.");
        }

        if (preferences == null) {
            String visitorSuffix = ((visitorFields == null) ? "anonymous" : visitorFields.getId());
            String encryptedPreferenceFilename = SHARED_PREFS_NAME + visitorSuffix + "-enc";
            try {
                MasterKey masterKey = new MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();

                preferences = EncryptedSharedPreferences.create(
                    context,
                    encryptedPreferenceFilename,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );

                boolean nonEncryptedPreferenceFilesRemoved = preferences.getBoolean(PREFS_KEY_NON_ENCRYPTED_PREFERENCES_REMOVED, false);
                if (!nonEncryptedPreferenceFilesRemoved) {
                    // remove all non-encrypted data
                    List<File> deprecatedNonEncryptedPreferences = getDeprecatedPreferencesFiles(context);
                    if (deprecatedNonEncryptedPreferences != null) {
                        for (File pref : deprecatedNonEncryptedPreferences) {
                            String filename = pref.getName().replaceFirst(".xml", "");
                            SharedPreferences nonEncryptedPreferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
                            clearVisitorData(context, nonEncryptedPreferences);
                            pref.delete();
                        }
                    }
                    preferences.edit().putBoolean(PREFS_KEY_NON_ENCRYPTED_PREFERENCES_REMOVED, true).apply();
                }
            } catch (GeneralSecurityException | IOException exception) {
                WebimInternalLog.getInstance().log(
                    "Error while opening EncryptedSharedPreferences: " + exception.getMessage(),
                    Webim.SessionBuilder.WebimLogVerbosityLevel.ERROR);
                if (errorHandler != null) {
                    errorHandler.onError(new WebimErrorImpl<>(FatalErrorType.GENERAL_SECURITY_ERROR, exception.getMessage()));
                }
                preferences = context.getSharedPreferences(SHARED_PREFS_NAME_DEPRECATED + visitorSuffix, Context.MODE_PRIVATE);
            }
        }

        String previousAccount = preferences.getString(PREFS_KEY_PREVIOUS_ACCOUNT, null);
        if (clearVisitorData || (previousAccount != null && !previousAccount.equals(accountName))) {
            clearVisitorData(context, preferences);
        }

        preferences.edit().putString(PREFS_KEY_PREVIOUS_ACCOUNT, accountName).apply();

        checkSavedSession(context, preferences, visitorFields);

        String serverUrl = InternalUtils.createServerUrl(accountName);

        Handler handler = new Handler();

        String pageId = preferences.getString(PREFS_KEY_PAGE_ID, null);

        String authToken = preferences.getString(PREFS_KEY_AUTH_TOKEN, null);

        MessageFactories.Mapper<MessageImpl> historyMessageMapper =
                new MessageFactories.MapperHistory(serverUrl, messageParsingErrorHandler);
        MessageFactories.Mapper<MessageImpl> currentChatMessageMapper =
                new MessageFactories.MapperCurrentChat(serverUrl, messageParsingErrorHandler);

        SessionDestroyerImpl sessionDestroyer = new SessionDestroyerImpl(context, preferences);
        AccessCheckerImpl accessChecker = new AccessCheckerImpl(Thread.currentThread(), sessionDestroyer);

        DeltaCallbackImpl deltaCallback = new DeltaCallbackImpl(
            currentChatMessageMapper,
            historyMessageMapper,
            preferences);

        final WebimClient client = new WebimClientBuilder()
            .setBaseUrl(serverUrl)
            .setLocation(location).setAppVersion(appVersion)
            .setVisitorFieldsJson((visitorFields == null) ? null : visitorFields.getJson())
            .setDeltaCallback(deltaCallback)
            .setSessionParamsListener(new SessionParamsListenerImpl(preferences))
            .setErrorListener(new DestroyIfNotErrorListener(sessionDestroyer,
                    new ErrorHandlerToInternalAdapter(errorHandler), notFatalErrorHandler))
            .setVisitorJson(preferences.getString(PREFS_KEY_VISITOR, null))
            .setProvidedAuthorizationListener(providedAuthorizationTokenStateListener)
            .setProvidedAuthorizationToken(providedAuthorizationToken)
            .setSessionId(preferences.getString(PREFS_KEY_SESSION_ID, null))
            .setAuthData(pageId != null ? new AuthData(pageId, authToken) : null)
            .setCallbackExecutor(new ExecIfNotDestroyedHandlerExecutor(sessionDestroyer,
                    handler))
            .setPlatform(PLATFORM)
            .setTitle((title != null) ? title : TITLE)
            .setPushToken(pushSystem,
                    pushSystem != Webim.PushSystem.NONE ? pushToken : null)
            .setDeviceId(getDeviceId(context, multivisitorSection))
            .setPrechatFields(prechatFields)
            .setSslSocketFactoryAndTrustManager(sslSocketFactory, trustManager)
            .setSessionCallback(sessionCallback)
            .build();

        FileUrlCreator fileUrlCreator = new FileUrlCreator(client, serverUrl);

        historyMessageMapper.setUrlCreator(fileUrlCreator);
        currentChatMessageMapper.setUrlCreator(fileUrlCreator);

        WebimActions actions = client.getActions();

        final HistoryStorage historyStorage;
        final HistoryMetaInfStorage historyMeta;
        if (storeHistoryLocally) {
            String dbName = preferences.getString(PREFS_KEY_HISTORY_DB_NAME, null);
            String dbPassword = preferences.getString(PREFS_KEY_HISTORY_DB_PASSWORD, null);
            if (dbName == null || dbPassword == null) {
                preferences.edit()
                    .putString(PREFS_KEY_HISTORY_DB_NAME, dbName = "webim_" + StringId.generateClientSide() + ".db")
                    .putString(PREFS_KEY_HISTORY_DB_PASSWORD, dbPassword = UUID.randomUUID().toString())
                    .apply();
            }
            historyMeta = new PreferencesHistoryMetaInfStorage(preferences);
            historyStorage = new SQLiteHistoryStorage(context,
                    handler,
                    dbName,
                    serverUrl,
                    historyMeta.isHistoryEnded(),
                    fileUrlCreator,
                    preferences.getLong(PREFS_KEY_READ_BEFORE_TIMESTAMP, -1),
                    dbPassword);
            if (preferences.getInt(PREFS_KEY_HISTORY_MAJOR_VERSION, -1)
                    != historyStorage.getMajorVersion()) {
                preferences.edit()
                    .remove(PREFS_KEY_HISTORY_REVISION)
                    .remove(PREFS_KEY_HISTORY_ENDED)
                    .putInt(PREFS_KEY_HISTORY_MAJOR_VERSION, historyStorage.getMajorVersion())
                    .apply();
            }
        } else {
            historyStorage = new MemoryHistoryStorage(preferences
                    .getLong(PREFS_KEY_READ_BEFORE_TIMESTAMP, -1));
            historyMeta = new MemoryHistoryMetaInfStorage();
        }

        final MessageHolder messageHolder = new MessageHolderImpl(
                accessChecker,
                new RemoteHistoryProviderImpl(actions, historyMessageMapper, historyMeta),
                historyStorage,
                historyMeta.isHistoryEnded()
        );

        MessageStreamImpl stream = new MessageStreamImpl(
                serverUrl,
                currentChatMessageMapper,
                new MessageFactories.SendingFactory(serverUrl, fileUrlCreator),
                new MessageFactories.OperatorFactory(serverUrl),
                new SurveyFactory(),
                accessChecker,
                actions,
                messageHolder,
                new MessageComposingHandlerImpl(handler, actions),
                new LocationSettingsHolder(preferences)
        );

        final HistoryPoller hPoller = new HistoryPoller(sessionDestroyer,
                historyMessageMapper,
                actions,
                messageHolder,
                handler,
                historyMeta);

        sessionDestroyer.addDestroyAction(new Runnable() {
            @Override
            public void run() {
                client.stop();
            }
        });
        sessionDestroyer.addDestroyAction(new Runnable() {
            @Override
            public void run() {
                hPoller.pause();
            }
        });

        LocationStatusPoller locationStatusPoller = null;
        if (requestLocationFrequency > 0) {
            locationStatusPoller = new LocationStatusPoller(actions, handler, stream, location, requestLocationFrequency);
            LocationStatusPoller finalLocationPoller = locationStatusPoller;
            sessionDestroyer.addDestroyAction(new Runnable() {
                @Override
                public void run() {
                    finalLocationPoller.pause();
                }
            });
        }

        WebimSessionImpl session = new WebimSessionImpl(
            accessChecker,
            sessionDestroyer,
            client,
            hPoller,
            stream,
            locationStatusPoller
        );

        deltaCallback.setStream(stream, messageHolder, session, hPoller);

        WebimInternalLog.getInstance().log("Specified Webim server – " + serverUrl,
                Webim.SessionBuilder.WebimLogVerbosityLevel.DEBUG);

        return session;
    }

    private static List<File> getDeprecatedPreferencesFiles(Context context) {
        File prefsDirectory = new File(context.getApplicationInfo().dataDir, "shared_prefs");
        if (prefsDirectory.exists() && prefsDirectory.isDirectory()) {
            File[] files = prefsDirectory.listFiles();
            if (files == null) {
                return null;
            }
            List<File> deprecatedPrefs = new ArrayList<>();
            for (File file : files) {
                if (file.getName().startsWith(SHARED_PREFS_NAME_DEPRECATED)) {
                    deprecatedPrefs.add(file);
                }
            }
            return deprecatedPrefs;
        }
        return null;
    }

    @NonNull
    private static String getDeviceId(@NonNull Context context, @NonNull String suffix) {
        SharedPreferences guidPrefs
                = context.getSharedPreferences(GUID_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        if (!suffix.isEmpty()) {
            suffix = "-" + suffix;
        }
        String name = "guid" + suffix;
        String guid = guidPrefs.getString(name, null);
        if (guid == null) {
            guid = UUID.randomUUID().toString() + suffix;
            guidPrefs.edit().putString(name, guid).apply();
        }

        return guid;
    }

    private static void clearVisitorData(@NonNull Context context,
                                         @NonNull SharedPreferences preferences) {
        String dbName = preferences.getString(PREFS_KEY_HISTORY_DB_NAME, null);
        if (dbName != null) {
            context.deleteDatabase(dbName);
        }
        preferences.edit().clear().apply();
    }

    private static void checkSavedSession(@NonNull Context context,
                                          @NonNull SharedPreferences preferences,
                                          @Nullable ProvidedVisitorFields newVisitorFields) {
        String newVisitorFieldsJson = newVisitorFields == null ? null : newVisitorFields.getJson();
        String oldVisitorFieldsJson = preferences.getString(PREFS_KEY_VISITOR_EXT, null);
        if (oldVisitorFieldsJson != null) {
            try {
                ProvidedVisitorFields oldVisitorFields
                        = new ProvidedVisitorFields(oldVisitorFieldsJson);
                if (newVisitorFields == null
                        || !oldVisitorFields.getId().equals(newVisitorFields.getId())) {
                    clearVisitorData(context, preferences);
                }
            } catch (Exception ignored) {
            }
        }
        if (!InternalUtils.equals(oldVisitorFieldsJson, newVisitorFieldsJson)) {
            preferences.edit()
                    .remove(PREFS_KEY_VISITOR)
                    .remove(PREFS_KEY_SESSION_ID)
                    .remove(PREFS_KEY_PAGE_ID)
                    .putString(PREFS_KEY_VISITOR_EXT, newVisitorFieldsJson)
                    .apply();
        }
    }

    private static class RemoteHistoryProviderImpl implements RemoteHistoryProvider {
        private final WebimActions actions;
        @NonNull
        private final MessageFactories.Mapper<MessageImpl> historyMessageMapper;
        private final HistoryMetaInfStorage historyMeta;

        private RemoteHistoryProviderImpl(WebimActions actions,
                                          @NonNull MessageFactories.Mapper<MessageImpl>
                                                  historyMessageMapper,
                                          HistoryMetaInfStorage historyMeta) {
            this.actions = actions;
            this.historyMessageMapper = historyMessageMapper;
            this.historyMeta = historyMeta;
        }

        @Override
        public void requestHistoryBefore(long beforeTs, final HistoryBeforeCallback callback) {
            actions.requestHistoryBefore(beforeTs, new DefaultCallback<HistoryBeforeResponse>() {
                @Override
                public void onSuccess(HistoryBeforeResponse response) {
                    HistoryBeforeResponse.HistoryResponseData data = response.getData();
                    if (data != null) {
                        List<MessageItem> list = data.getMessages();
                        boolean hasMore = data.getHasMore();
                        callback.onSuccess(historyMessageMapper.mapAll(list), hasMore);
                        if (!hasMore) {
                            historyMeta.setHistoryEnded(true);
                        }
                    } else {
                        callback.onSuccess(Collections.<MessageImpl>emptyList(), false);
                        historyMeta.setHistoryEnded(true);
                    }
                }
            });
        }
    }

    private interface HistoryMetaInfStorage {
        @Nullable
        String getRevision();

        void setRevision(@Nullable String revision);

        boolean isHistoryEnded();

        void setHistoryEnded(boolean historyEnded);

        void clear();
    }

    private static class PreferencesHistoryMetaInfStorage implements HistoryMetaInfStorage {
        @NonNull
        private final SharedPreferences preferences;

        private PreferencesHistoryMetaInfStorage(@NonNull SharedPreferences preferences) {
            this.preferences = preferences;
        }

        @Override
        public @Nullable
        String getRevision() {
            return preferences.getString(PREFS_KEY_HISTORY_REVISION, null);
        }

        @Override
        public void setRevision(@Nullable String revision) {
            preferences.edit().putString(PREFS_KEY_HISTORY_REVISION, revision).apply();
        }

        @Override
        public boolean isHistoryEnded() {
            return preferences.getBoolean(PREFS_KEY_HISTORY_ENDED, false);
        }

        @Override
        public void setHistoryEnded(boolean historyEnded) {
            preferences.edit().putBoolean(PREFS_KEY_HISTORY_ENDED, historyEnded).apply();
        }

        @Override
        public void clear() {
            preferences.edit()
                    .remove(PREFS_KEY_HISTORY_REVISION)
                    .remove(PREFS_KEY_HISTORY_ENDED)
                    .apply();
        }
    }

    private static class MemoryHistoryMetaInfStorage implements HistoryMetaInfStorage {
        @Nullable
        private String revision;
        private boolean historyEnded;

        @Override
        @Nullable
        public String getRevision() {
            return revision;
        }

        @Override
        public void setRevision(@Nullable String revision) {
            this.revision = revision;
        }

        @Override
        public boolean isHistoryEnded() {
            return historyEnded;
        }

        @Override
        public void setHistoryEnded(boolean historyEnded) {
            this.historyEnded = historyEnded;
        }

        @Override
        public void clear() {
            revision = null;
            historyEnded = false;
        }
    }

    private static class HistoryPoller {
        private static final int HISTORY_POLL_INTERVAL = 60_000;
        @NonNull
        private final SessionDestroyer destroyer;
        @NonNull
        private final MessageFactories.Mapper<MessageImpl> historyMessageMapper;
        @NonNull
        private final WebimActions actions;
        @NonNull
        private final MessageHolder messageHolder;
        @NonNull
        private final Handler handler;
        @NonNull
        private final HistoryMetaInfStorage historyMeta;

        @NonNull
        private final HistorySinceCallback historySinceCallback;

        private boolean running;
        @Nullable
        private Runnable callback;
        private long lastPollTime = -HISTORY_POLL_INTERVAL;
        @Nullable
        private String lastRevision;
        private boolean hasHistoryRevisionDelta;

        private HistoryPoller(
                @NonNull SessionDestroyer destroyer,
                @NonNull MessageFactories.Mapper<MessageImpl> historyMessageMapper,
                @NonNull WebimActions actions,
                @NonNull MessageHolder messageHolder,
                @NonNull Handler handler,
                @NonNull HistoryMetaInfStorage historyMeta
        ) {
            this.destroyer = destroyer;
            this.historyMessageMapper = historyMessageMapper;
            this.actions = actions;
            this.messageHolder = messageHolder;
            this.handler = handler;
            this.historyMeta = historyMeta;
            this.lastRevision = historyMeta.getRevision();
            this.historySinceCallback = createHistoryCallback();
        }

        private void setHasHistoryRevisionDelta(boolean hasHistoryRevisionDelta) {
            this.hasHistoryRevisionDelta = hasHistoryRevisionDelta;
        }

        private void insertMessageInDB(MessageImpl message) {
            if (destroyer.isDestroyed()) {
                return;
            }
            List<MessageImpl> messages = new ArrayList<>();
            messages.add(message);
            messageHolder.receiveHistoryUpdate(messages, Collections.<String>emptySet(), new Runnable() {
                @Override
                public void run() {
                    // Ревизия сохраняется только по окончанию записи истории.
                    // Так, если история не сможет сохраниться,
                    // ревизия не будет перезаписана и история будет перезапрошена.
                    historyMeta.setRevision(lastRevision);
                }
            });
        }

        private void updateReadBeforeTimestamp(long timestamp) {
            messageHolder.updateReadBeforeTimestamp(timestamp);
        }

        private void deleteMessageFromDB(String message) {
            if (destroyer.isDestroyed()) {
                return;
            }
            Set<String> messages = new HashSet<>();
            messages.add(message);
            messageHolder.receiveHistoryUpdate(Collections.<MessageImpl>emptyList(),
                    messages,
                    new Runnable() {
                @Override
                public void run() {
                    // Ревизия сохраняется только по окончанию записи истории.
                    // Так, если история не сможет сохраниться,
                    // ревизия не будет перезаписана и история будет перезапрошена.
                    historyMeta.setRevision(lastRevision);
                }
            });
        }

        private HistorySinceCallback createHistoryCallback() {
            return new HistorySinceCallback() {
                @Override
                public void onSuccess(
                        List<MessageImpl> messages,
                        Set<String> deleted,
                        final boolean hasMore,
                        final boolean isInitial,
                        final @Nullable String revision
                ) {
                    if (destroyer.isDestroyed()) {
                        return;
                    }
                    lastPollTime = SystemClock.uptimeMillis();
                    lastRevision = revision;
                    messageHolder.receiveHistoryUpdate(messages, deleted, new Runnable() {
                        @Override
                        public void run() {
                            // Ревизия сохраняется только по окончанию записи истории.
                            // Так, если история не сможет сохраниться,
                            // ревизия не будет перезаписана и история будет перезапрошена.
                            historyMeta.setRevision(revision);
                            if (isInitial && !hasMore) {
                                messageHolder.setReachedEndOfRemoteHistory(true);
                                historyMeta.setHistoryEnded(true);
                            }
                        }
                    });
                    if (!running) {
                        if (!isInitial && hasMore) {
                            lastPollTime = -HISTORY_POLL_INTERVAL;
                        }
                        return;
                    }

                    if (!isInitial && hasMore) {
                        requestHistorySince(revision, this);
                    } else {
                        if (!hasHistoryRevisionDelta) {
                            handler.postDelayed(callback = createRequestRunnable(revision),
                                    HISTORY_POLL_INTERVAL);
                        }
                    }
                }
            };
        }

        private Runnable createRequestRunnable(final @Nullable String revision) {
            return new Runnable() {
                @Override
                public void run() {
                    if (!hasHistoryRevisionDelta) {
                        requestHistorySince(revision, historySinceCallback);
                    }
                }
            };
        }

        public void pause() {
            if (callback != null) {
                handler.removeCallbacks(callback);
            }
            callback = null;
            running = false;
        }

        public void resume() {
            pause();
            running = true;
            if (SystemClock.uptimeMillis() - lastPollTime > HISTORY_POLL_INTERVAL) {
                requestHistorySince(lastRevision, historySinceCallback);
            } else {
                if (!hasHistoryRevisionDelta) {
                    handler.postAtTime(callback = createRequestRunnable(lastRevision),
                            lastPollTime + HISTORY_POLL_INTERVAL);
                }
            }
        }

        public void restart() {
            pause();
            lastPollTime = -HISTORY_POLL_INTERVAL;
            lastRevision = historyMeta.getRevision();
            resume();

        }

        public void requestHistorySince(String revision) {
            lastRevision = historyMeta.getRevision();
            if (lastRevision == null || !lastRevision.equals(revision)) {
                requestHistorySince(lastRevision, historySinceCallback);
            }
        }

        private void requestHistorySince(@Nullable String since,
                                         final HistorySinceCallback callback) {
            actions.requestHistorySince(since, wrapHistorySinceCallback(since, callback));
        }

        private DefaultCallback<HistorySinceResponse> wrapHistorySinceCallback(
                @Nullable final String since,
                final HistorySinceCallback callback) {
            return new DefaultCallback<HistorySinceResponse>() {
                @Override
                public void onSuccess(HistorySinceResponse response) {
                    HistorySinceResponse.HistoryResponseData data = response.getData();
                    if (data == null) {
                        callback.onSuccess(Collections.<MessageImpl>emptyList(),
                                Collections.<String>emptySet(), false, since == null, since);
                    } else {
                        List<MessageItem> list = data.getMessages();
                        List<MessageItem> changes = new ArrayList<>(list.size());
                        Set<String> deletes = new HashSet<>();
                        for (MessageItem msg : list) {
                            if (msg.isDeleted()) {
                                deletes.add(msg.getId());
                            } else {
                                changes.add(msg);
                            }
                        }
                        data.getRevision().getClass(); // NPE
                        callback.onSuccess(historyMessageMapper.mapAll(changes),
                                deletes,
                                data.getHasMore(),
                                since == null,
                                data.getRevision());
                    }
                }
            };
        }
    }

    private static class LocationStatusPoller {
        @NonNull
        private final WebimActions actions;
        @NonNull
        private final Handler handler;
        @NonNull
        private final MessageStreamImpl messageStream;
        @NonNull
        private final String location;
        private final long requestLocationFrequency;

        @Nullable
        private Runnable callback;

        public LocationStatusPoller(
            @NonNull WebimActions actions,
            @NonNull Handler handler,
            @NonNull MessageStreamImpl messageStream,
            @NonNull String location,
            long requestLocationFrequency) {
            this.actions = actions;
            this.handler = handler;
            this.messageStream = messageStream;
            this.location = location;
            this.requestLocationFrequency = requestLocationFrequency;
        }

        public void resume() {
            pause();
            callback = createCallback();
            handler.post(callback);
        }

        public void pause() {
            if (callback != null) {
                handler.removeCallbacks(callback);
            }
            callback = null;
        }


        private Runnable createCallback() {
            return new Runnable() {
                @Override
                public void run() {
                    requestLocationStatus();
                }
            };
        }

        private void requestLocationStatus() {
            actions.getLocationStatus(location, new DefaultCallback<LocationStatusResponse>() {
                @Override
                public void onSuccess(LocationStatusResponse response) {
                    messageStream.setOnlineStatus(response.getOnlineStatus());
                    handler.postDelayed(callback, requestLocationFrequency);
                }
            });
        }
    }

    private static class SessionParamsListenerImpl implements SessionParamsListener {
        @NonNull
        private final SharedPreferences preferences;

        @Nullable
        private Runnable onVisitorIdChangeListener;

        private SessionParamsListenerImpl(@NonNull SharedPreferences preferences) {
            this.preferences = preferences;
        }

        public void setOnVisitorIdChangeListener(@NonNull Runnable onVisitorIdChangeListener) {
            onVisitorIdChangeListener.getClass(); // NPE
            this.onVisitorIdChangeListener = onVisitorIdChangeListener;
        }

        @Override
        public void onSessionParamsChanged(@NonNull String visitorJson,
                                           @NonNull String sessionId,
                                           @NonNull AuthData authData) {
            String oldVisitorFieldsJson = preferences.getString(PREFS_KEY_VISITOR, null);
            if (onVisitorIdChangeListener != null
                    && oldVisitorFieldsJson != null
                    && !oldVisitorFieldsJson.equals(visitorJson)) {
                try {
                    String oldId = new JsonParser()
                            .parse(oldVisitorFieldsJson)
                            .getAsJsonObject()
                            .get("id")
                            .getAsString();
                    String newId = new JsonParser()
                            .parse(visitorJson)
                            .getAsJsonObject()
                            .get("id")
                            .getAsString();
                    if (oldId != null && !oldId.equals(newId)) {
                        onVisitorIdChangeListener.run();
                    }
                } catch (Exception ignored) {
                }
            }
            preferences.edit()
                    .putString(PREFS_KEY_VISITOR, visitorJson)
                    .putString(PREFS_KEY_SESSION_ID, sessionId)
                    .putString(PREFS_KEY_PAGE_ID, authData.getPageId())
                    .putString(PREFS_KEY_AUTH_TOKEN, authData.getAuthToken())
                    .apply();
        }
    }

    private static class DeltaCallbackImpl implements DeltaCallback {
        @NonNull
        private final MessageFactories.Mapper<MessageImpl> currentChatMessageMapper;
        @NonNull
        private final MessageFactories.Mapper<MessageImpl> historyChatMessageMapper;
        @Nullable
        private ChatItem currentChat;
        @NonNull
        private HistoryPoller historyPoller;
        @NonNull
        private SharedPreferences preferences;
        private MessageStreamImpl messageStream;
        private MessageHolder messageHolder;
        private WebimSessionImpl session;
        private boolean firstFullUpdateReceived;

        private DeltaCallbackImpl(
                @NonNull MessageFactories.Mapper<MessageImpl> currentChatMessageMapper,
                @NonNull MessageFactories.Mapper<MessageImpl> historyChatMessageMapper,
                @NonNull SharedPreferences preferences) {
            this.currentChatMessageMapper = currentChatMessageMapper;
            this.historyChatMessageMapper = historyChatMessageMapper;
            this.preferences = preferences;
        }

        public void setStream(MessageStreamImpl stream,
                              MessageHolder messageHolder,
                              WebimSessionImpl session,
                              HistoryPoller historyPoller) {
            this.messageStream = stream;
            this.messageHolder = messageHolder;
            this.session = session;
            this.historyPoller = historyPoller;
        }

        @Override
        public void onFullUpdate(@NonNull DeltaFullUpdate fullUpdate) {
            final List<DepartmentItem> departmentItemList = fullUpdate.getDepartments();
            if (departmentItemList != null) {
                messageStream.onReceivingDepartmentList(departmentItemList);
            }
            messageStream.setInvitationState(VisitSessionStateItem.getType(fullUpdate.getState()));
            currentChat = fullUpdate.getChat();
            messageStream.handleGreetingMessage(
                    fullUpdate.getShowHelloMessage(),
                    fullUpdate.getChatStartAfterMessage(),
                    currentChat == null || currentChat.getMessages().isEmpty(),
                    fullUpdate.getHelloMessageDescr());
            if (fullUpdate.getSurvey() != null) {
                messageStream.onSurveyReceived(fullUpdate.getSurvey());
            }
            messageStream.onFullUpdate(currentChat);
            messageStream.saveLocationSettings(fullUpdate);

            String status = fullUpdate.getOnlineStatus();
            messageStream.setOnlineStatus(status);

            String revision = fullUpdate.getHistoryRevision();
            if (revision != null) {
                historyPoller.setHasHistoryRevisionDelta(true);
                historyPoller.requestHistorySince(revision);
            }

            if (currentChat != null) {
                for (MessageItem messageItem : currentChat.getMessages()) {
                    MessageImpl message = historyChatMessageMapper.map(messageItem);
                    if (message != null) {
                        historyPoller.insertMessageInDB(message);
                    }
                    if (message != null
                            && (message.getType() == Message.Type.VISITOR
                            || message.getType() == Message.Type.FILE_FROM_VISITOR)
                            && message.isReadByOperator()) {
                        long time = message.getTimeMicros();
                        if (time > preferences.getLong(PREFS_KEY_READ_BEFORE_TIMESTAMP, -1)) {
                            preferences.edit()
                                    .putLong(PREFS_KEY_READ_BEFORE_TIMESTAMP, time)
                                    .apply();
                            historyPoller.updateReadBeforeTimestamp(time);
                        }
                    }
                }
            } else {
                preferences.edit()
                        .putLong(PREFS_KEY_READ_BEFORE_TIMESTAMP, -1)
                        .apply();
            }

            if (!firstFullUpdateReceived) {
                firstFullUpdateReceived = true;
                messageHolder.onFirstFullUpdateReceived();
            }
        }

        @Override
        public void onDeltaList(@NonNull List<DeltaItem> list) {
            if ((messageStream == null) || (messageHolder == null)) {
                throw new IllegalStateException();
            }

            for (DeltaItem deltaItem : list) {
                DeltaItem.Type deltaType = deltaItem.getObjectType();
                if (deltaType == null) {
                    continue;
                }

                switch (deltaType) {
                    case CHAT: {
                        handleChatDelta(deltaItem);
                        break;
                    }
                    case CHAT_MESSAGE: {
                        handleChatMessageDelta(deltaItem);
                        break;
                    }
                    case CHAT_OPERATOR: {
                        handleChatOperatorDelta(deltaItem);
                        break;
                    }
                    case CHAT_OPERATOR_TYPING: {
                        handleChatOperatorTypingDelta(deltaItem);
                        break;
                    }
                    case CHAT_READ_BY_VISITOR: {
                        handleChatReadByVisitorDelta(deltaItem);
                        break;
                    }
                    case CHAT_STATE: {
                        handleChatStateDelta(deltaItem);
                        break;
                    }
                    case CHAT_UNREAD_BY_OPERATOR_SINCE_TIMESTAMP: {
                        handleChatUnreadByOperatorSinceTimestampDelta(deltaItem);

                        break;
                    }
                    case DEPARTMENT_LIST: {
                        handleDepartmentListDelta(deltaItem);
                        break;
                    }
                    case HISTORY_REVISION: {
                        handleHistoryRevision(deltaItem);
                        break;
                    }
                    case OPERATOR_RATE: {
                        handleOperatorRateDelta(deltaItem);
                        break;
                    }
                    case READ_MESSAGE: {
                        handleMessageRead(deltaItem);
                        break;
                    }
                    case UNREAD_BY_VISITOR: {
                        handlerUnreadByVisitor(deltaItem);
                        break;
                    }
                    case VISIT_SESSION_STATE: {
                        handleVisitSessionStateDelta(deltaItem);
                        break;
                    }
                    case SURVEY: {
                        handleSurveyDelta(deltaItem);
                    }
                    default: {
                        break;
                    }
                }
            }
        }

        private void handleChatDelta(DeltaItem deltaItem) {
            if (deltaItem.getEvent() != DeltaItem.Event.UPDATE) {
                return;
            }

            currentChat = (ChatItem) deltaItem.getData();
            if ((currentChat != null) && currentChat.isReadByVisitor()) {
                currentChat.setUnreadByVisitorTimestamp(0);
            }

            messageStream.onChatUpdated(currentChat, false);
        }

        private void handleChatMessageDelta(DeltaItem deltaItem) {
            DeltaItem.Event deltaEvent = deltaItem.getEvent();

            if (deltaEvent == DeltaItem.Event.DELETE) {
                MessageImpl message = null, historyMessage = null;
                if (currentChat != null) {
                    for (ListIterator<MessageItem> iterator
                         = currentChat.getMessages().listIterator(); iterator.hasNext(); ) {
                        MessageItem messageItem = iterator.next();
                        if (messageItem.getId().equals(deltaItem.getId())) {
                            message = currentChatMessageMapper.map(messageItem);
                            historyMessage = historyChatMessageMapper.map(messageItem);
                            iterator.remove();

                            break;
                        }
                    }
                }

                if (message != null && historyMessage != null) {
                    messageHolder.onMessageDeleted(deltaItem.getId());
                    historyPoller.deleteMessageFromDB(historyMessage.getServerSideId());
                }
            } else {
                MessageItem messageItem = (MessageItem) deltaItem.getData();
                MessageImpl message = currentChatMessageMapper.map(messageItem);
                MessageImpl historyMessage = historyChatMessageMapper.map(messageItem);
                if (deltaEvent == DeltaItem.Event.ADD) {
                    boolean isNewMessage = false;
                    if (currentChat != null) {
                        if (!currentChat.getMessages().contains(messageItem)) {
                            currentChat.addMessage(messageItem);
                            isNewMessage = true;
                        }
                    }

                    if (message != null && isNewMessage) {
                        messageHolder.onMessageAdded(message);
                    }
                    if (historyMessage != null) {
                        historyPoller.insertMessageInDB(historyMessage);
                    }
                } else if (deltaEvent == DeltaItem.Event.UPDATE) {
                    if (currentChat != null) {
                        for (ListIterator<MessageItem> iterator
                             = currentChat.getMessages().listIterator(); iterator.hasNext(); ) {
                            if (iterator.next().getId().equals(messageItem.getId())) {
                                iterator.set(messageItem);

                                break;
                            }
                        }
                    }

                    if (message != null) {
                        messageHolder.onMessageChanged(message);
                    }
                    historyPoller.insertMessageInDB(historyMessage);
                }
            }
        }

        private void handleChatOperatorDelta(DeltaItem deltaItem) {
            if (deltaItem.getEvent() != DeltaItem.Event.UPDATE) {
                return;
            }

            OperatorItem operatorItem = (OperatorItem) deltaItem.getData();
            if (currentChat != null) {
                currentChat.setOperator(operatorItem);
            }

            messageStream.onOperatorUpdated(currentChat);
        }

        private void handleChatOperatorTypingDelta(DeltaItem deltaItem) {
            if (deltaItem.getEvent() != DeltaItem.Event.UPDATE) {
                return;
            }

            boolean isTyping = (Boolean) deltaItem.getData();
            if (currentChat != null) {
                currentChat.setOperatorTyping(isTyping);
            }

            messageStream.onOperatorTypingUpdated(currentChat);
        }

        private void handleChatReadByVisitorDelta(DeltaItem deltaItem) {
            if (deltaItem.getEvent() != DeltaItem.Event.UPDATE) {
                return;
            }

            boolean isRead = (Boolean) deltaItem.getData();

            if (currentChat != null) {
                currentChat.setReadByVisitor(isRead);
                if (isRead) {
                    currentChat.setUnreadByVisitorTimestamp(0);
                }
            }

            if (isRead) {
                messageStream.setUnreadByVisitorTimestamp(0);
            }
        }

        private void handleChatStateDelta(DeltaItem deltaItem) {
            if (deltaItem.getEvent() != DeltaItem.Event.UPDATE) {
                return;
            }

            String state = (String) deltaItem.getData();
            if (currentChat != null) {
                currentChat.setState(state);
            }

            messageStream.onChatStateUpdated(currentChat);
        }

        private void handleChatUnreadByOperatorSinceTimestampDelta(DeltaItem deltaItem) {
            if (deltaItem.getEvent() != DeltaItem.Event.UPDATE) {
                return;
            }

            Object delta = deltaItem.getData();
            if (delta != null) {
                double unreadByOperatorTimestamp = (double) delta;

                if (currentChat != null) {
                    currentChat.setUnreadByOperatorTimestamp(unreadByOperatorTimestamp);
                }

                messageStream
                        .setUnreadByOperatorTimestamp((long) (unreadByOperatorTimestamp * 1000L));
            } else {
                if (currentChat != null) {
                    currentChat.setUnreadByOperatorTimestamp(0);
                }

                messageStream.setUnreadByOperatorTimestamp(0);
            }
        }

        private void handleDepartmentListDelta(DeltaItem deltaItem) {
            List<DepartmentItem> departmentItemList = (List<DepartmentItem>) deltaItem.getData();
            messageStream.onReceivingDepartmentList(departmentItemList);
        }

        private void handleOperatorRateDelta(DeltaItem deltaItem) {
            if (deltaItem.getEvent() != DeltaItem.Event.UPDATE) {
                return;
            }

            RatingItem rating = (RatingItem) deltaItem.getData();
            if (currentChat != null) {
                String operatorId = rating.getOperatorId();
                if (operatorId != null) {
                    currentChat.getOperatorIdToRating().put(rating.getOperatorId(), rating);
                }
            }
        }

        private void handleHistoryRevision(DeltaItem deltaItem) {
            if (deltaItem.getEvent() != DeltaItem.Event.UPDATE) {
                return;
            }

            HistoryRevisionItem revisionItem = (HistoryRevisionItem) deltaItem.getData();
            historyPoller.requestHistorySince(revisionItem.getRevision());
        }

        private void handleMessageRead(DeltaItem deltaItem) {
            DeltaItem.Event deltaEvent = deltaItem.getEvent();
            String id = deltaItem.getId();
            Object data = deltaItem.getData();
            if (data instanceof Boolean && deltaEvent == DeltaItem.Event.UPDATE) {
                boolean isRead = (Boolean) data;
                if (currentChat != null) {
                    for (ListIterator<MessageItem> iterator
                         = currentChat.getMessages().listIterator(); iterator.hasNext(); ) {
                        MessageItem messageItem = iterator.next();
                        if (messageItem.getId().equals(id)) {
                            messageItem.setRead(isRead);
                            MessageImpl message = currentChatMessageMapper.map(messageItem);
                            iterator.set(messageItem);
                            if (message != null) {
                                messageHolder.onMessageChanged(message);
                                long time = message.getTimeMicros();
                                if (time > preferences
                                        .getLong(PREFS_KEY_READ_BEFORE_TIMESTAMP, -1)) {
                                    preferences.edit()
                                            .putLong(PREFS_KEY_READ_BEFORE_TIMESTAMP,
                                                    message.getTimeMicros())
                                            .apply();
                                    historyPoller.updateReadBeforeTimestamp(time);
                                }
                            }

                            break;
                        }
                    }
                }
            }
        }

        private void handlerUnreadByVisitor(DeltaItem deltaItem) {
            if (deltaItem.getEvent() != DeltaItem.Event.UPDATE) {
                return;
            }

            Object delta = deltaItem.getData();
            if (delta != null) {
                UnreadByVisitorMessagesItem item = (UnreadByVisitorMessagesItem) delta;

                if (currentChat != null) {
                    currentChat.setUnreadByVisitorTimestamp(item.getSinceTs());
                    currentChat.setUnreadByVisitorMessageCount(item.getMessageCount());
                }

                messageStream
                        .setUnreadByVisitorTimestamp((long) (item.getSinceTs() * 1000L));
                messageStream.setUnreadByVisitorMessageCount(item.getMessageCount());
            } else {
                if (currentChat != null) {
                    currentChat.setUnreadByVisitorTimestamp(0);
                    currentChat.setUnreadByVisitorMessageCount(0);
                }

                messageStream.setUnreadByVisitorTimestamp(0);
                messageStream.setUnreadByVisitorMessageCount(0);
            }

        }

        private void handleVisitSessionStateDelta(DeltaItem deltaItem) {
            String sessionState = (String) deltaItem.getData();

            if (sessionState.equals(VisitSessionStateItem.OFFLINE_MESSAGE.getTypeValue())) {
                messageStream.setOnlineStatus(OnlineStatusItem.OFFLINE.getTypeValue());
                session.client.getActions().closeChat();
            }

            if (deltaItem.getEvent() == DeltaItem.Event.UPDATE) {
                messageStream.setInvitationState(VisitSessionStateItem.getType(sessionState));
            }
        }

        private void handleSurveyDelta(DeltaItem deltaItem) {
            SurveyItem surveyItem = (SurveyItem) deltaItem.getData();
            if (surveyItem != null) {
                messageStream.onSurveyReceived(surveyItem);
            } else {
                messageStream.onSurveyCancelled();
            }
        }
    }

    private static class DestroyIfNotErrorListener implements InternalErrorListener {
        @Nullable
        private final SessionDestroyer destroyer;
        @Nullable
        private final InternalErrorListener errorListener;
        @Nullable
        private final NotFatalErrorHandler notFatalErrorHandler;

        private DestroyIfNotErrorListener(@Nullable SessionDestroyer destroyer,
                                          @Nullable InternalErrorListener errorListener,
                                          @Nullable NotFatalErrorHandler notFatalErrorHandler) {
            this.destroyer = destroyer;
            this.errorListener = errorListener;
            this.notFatalErrorHandler = notFatalErrorHandler;
        }

        @Override
        public void onError(@NonNull String url, @Nullable String error, int httpCode) {
            if (destroyer == null || !destroyer.isDestroyed()) {
                if (destroyer != null) {
                    destroyer.destroy();
                }
                if (errorListener != null) {
                    errorListener.onError(url, error, httpCode);
                }
            }
        }

        @Override
        public void onNotFatalError(@NonNull NotFatalErrorHandler.NotFatalErrorType error) {
            if (notFatalErrorHandler != null) {
                notFatalErrorHandler.onNotFatalError(new WebimErrorImpl<>(error, null));
            }
        }
    }

    private static class ErrorHandlerToInternalAdapter implements InternalErrorListener {
        @Nullable
        private final FatalErrorHandler errorHandler;

        private ErrorHandlerToInternalAdapter(@Nullable FatalErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
        }

        @Override
        public void onError(@NonNull String url, @Nullable String error, int httpCode) {
            if (errorHandler != null) {
                errorHandler.onError(new WebimErrorImpl<>(toPublicErrorType(error),
                        error != null
                                ? error
                                : "Server responded HTTP code: " + httpCode + " from URL: " + url));
            }
        }

        @Override
        public void onNotFatalError(@NonNull NotFatalErrorHandler.NotFatalErrorType error) {
        }

        @NonNull
        private static FatalErrorType toPublicErrorType(@Nullable String error) {
            if (error == null) {
                return FatalErrorType.UNKNOWN;
            }
            if (error.equals(WebimInternalError.ACCOUNT_BLOCKED)) {
                return FatalErrorType.ACCOUNT_BLOCKED;
            }
            if (error.equals(WebimInternalError.VISITOR_BANNED)) {
                return FatalErrorType.VISITOR_BANNED;
            }
            if (error.equals(WebimInternalError.WRONG_PROVIDED_VISITOR_HASH)) {
                return FatalErrorType.WRONG_PROVIDED_VISITOR_HASH;
            }
            if (error.equals(WebimInternalError.PROVIDED_VISITOR_EXPIRED)) {
                return FatalErrorType.PROVIDED_VISITOR_EXPIRED;
            }
            if (error.equals(DeltaRequestLoop.INCORRECT_SERVER_ANSWER)) {
                return FatalErrorType.INCORRECT_SERVER_ANSWER;
            }
            return FatalErrorType.UNKNOWN;
        }
    }

    private interface SessionDestroyer {
        boolean isDestroyed();

        void destroy();

        void destroyAndClearVisitorData();
    }

    private static class SessionDestroyerImpl implements SessionDestroyer {
        @NonNull
        private final List<Runnable> actions = new ArrayList<>();
        private boolean destroyed;
        private final Context context;
        private final SharedPreferences sharedPreferences;

        public SessionDestroyerImpl(Context context, SharedPreferences sharedPreferences) {
            this.context = context;
            this.sharedPreferences = sharedPreferences;
        }

        public void addDestroyAction(Runnable action) {
            actions.add(action);
        }

        @Override
        public boolean isDestroyed() {
            return destroyed;
        }

        @Override
        public void destroy() {
            if (!destroyed) {
                destroyed = true;
                for (Runnable action : actions) {
                    action.run();
                }
            }
        }

        @Override
        public void destroyAndClearVisitorData() {
            if (!destroyed) {
                destroyed = true;
                for (Runnable action : actions) {
                    action.run();
                }
                clearVisitorData(context, sharedPreferences);
            }
        }
    }

    private static class AccessCheckerImpl implements AccessChecker {

        @NonNull
        private final Thread thread;
        @NonNull
        private final SessionDestroyer destroyer;

        public AccessCheckerImpl(@NonNull Thread thread, @NonNull SessionDestroyer destroyer) {
            this.thread = thread;
            this.destroyer = destroyer;
        }

        @Override
        public void checkAccess() {
            if (thread != Thread.currentThread()) {
                throw new RuntimeException("All Webim actions must be invoked from"
                        + " thread on which the session has been created. " +
                        "Created on: " + thread + ", current thread: " + Thread.currentThread());
            }
            if (destroyer.isDestroyed()) {
                throw new IllegalStateException("Can't use destroyed session");
            }
        }
    }

    private static class ExecIfNotDestroyedHandlerExecutor implements Executor {
        private final SessionDestroyer destroyed;
        private final Handler handled;

        private ExecIfNotDestroyedHandlerExecutor(SessionDestroyer destroyed, Handler handled) {
            this.destroyed = destroyed;
            this.handled = handled;
        }

        @Override
        public void execute(final @NonNull Runnable command) {
            if (!destroyed.isDestroyed()) {
                handled.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!destroyed.isDestroyed()) {
                            command.run();
                        }
                    }
                });
            }
        }
    }
}
