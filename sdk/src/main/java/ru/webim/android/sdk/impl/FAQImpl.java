package ru.webim.android.sdk.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.webim.android.sdk.FAQ;
import ru.webim.android.sdk.FAQCategory;
import ru.webim.android.sdk.FAQItem;
import ru.webim.android.sdk.FAQSearchItem;
import ru.webim.android.sdk.FAQStructure;
import ru.webim.android.sdk.NotFatalErrorHandler;
import ru.webim.android.sdk.impl.backend.DefaultCallback;
import ru.webim.android.sdk.impl.backend.FAQClient;
import ru.webim.android.sdk.impl.backend.FAQClientBuilder;
import ru.webim.android.sdk.impl.backend.InternalErrorListener;
import ru.webim.android.sdk.impl.items.FAQCategoryItem;
import ru.webim.android.sdk.impl.items.FAQItemItem;
import ru.webim.android.sdk.impl.items.FAQSearchItemItem;
import ru.webim.android.sdk.impl.items.FAQStructureItem;
import ru.webim.android.sdk.impl.items.responses.DefaultResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public class FAQImpl implements FAQ {
    @NonNull
    private final AccessChecker accessChecker;
    @Nullable
    private final String application;
    @NonNull
    private final FAQClient client;
    @Nullable
    private final String departmentKey;
    @NonNull
    private final FAQDestroyer destroyer;
    private boolean clientStarted;
    @NonNull
    private FAQSQLiteHistoryStorage cache;
    @NonNull
    private final String deviceId;
    @Nullable
    private final String language;

    private FAQImpl(
            @NonNull AccessChecker accessChecker,
            @Nullable String application,
            @Nullable String departmentKey,
            @NonNull FAQDestroyer destroyer,
            @NonNull FAQClient client,
            @NonNull FAQSQLiteHistoryStorage cache,
            @NonNull String deviceId,
            @Nullable String language
    ) {
        this.accessChecker = accessChecker;
        this.application = application;
        this.departmentKey = departmentKey;
        this.destroyer = destroyer;
        this.client = client;
        this.cache = cache;
        this.deviceId = deviceId;
        this.language = language;
    }

    private static @NonNull
    String getDeviceId() {

        return UUID.randomUUID().toString();
    }


    public static FAQ newInstance(String accountName,
                                  String application,
                                  Context context,
                                  String departmentKey,
                                  String language,
                                  SSLSocketFactory sslSocketFactory,
                                  X509TrustManager trustManager) {
        accountName.getClass(); // NPE

        if (Looper.myLooper() == null) {
            throw new RuntimeException("The Thread on which Webim session creates " +
                    "should have attached android.os.Looper object.");
        }

        String serverUrl = InternalUtils.createServerUrl(accountName);

        Handler handler = new Handler();

        FAQDestroyer destroyer = new FAQDestroyer();
        AccessCheckerImpl accessChecker =
                new AccessCheckerImpl(Thread.currentThread(), destroyer);


        final FAQClient client = new FAQClientBuilder()
                .setBaseUrl(serverUrl)
                .setCallbackExecutor(new ExecIfNotDestroyedHandlerExecutor(destroyer, handler))
                .setSslSocketFactoryAndTrustManager(sslSocketFactory, trustManager)
                .build();

        destroyer.addDestroyAction(new Runnable() {
            @Override
            public void run() {
                client.stop();
            }
        });

        FAQSQLiteHistoryStorage cache
                = new FAQSQLiteHistoryStorage(context,handler, "faqcache.db");

        return new FAQImpl(
                accessChecker,
                application,
                departmentKey,
                destroyer,
                client,
                cache,
                getDeviceId(),
                language);
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
    }

    @Override
    public void pause() {
        if (destroyer.isDestroyed()) {
            return;
        }
        checkAccess();
        client.pause();
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
    public void getStructure(final String id, final GetCallback<FAQStructure> callback) {
        accessChecker.checkAccess();

        client.getActions().getStructure(id, new DefaultCallback<FAQStructureItem>() {
            @Override
            public void onSuccess(FAQStructureItem response) {
                if (response == null) {
                    callback.onError();
                } else {
                    callback.receive(response);
                    cache.insertStructure(id, response);
                }
            }
        });
    }

    @Override
    public void getCachedStructure(String id, GetCallback<FAQStructure> callback) {
        accessChecker.checkAccess();

        cache.getStructure(id, callback);
    }

    @Override
    public void getCategory(final String id, final GetCallback<FAQCategory> callback) {
        accessChecker.checkAccess();

        client.getActions().getCategory(id, deviceId, new DefaultCallback<FAQCategoryItem>() {
            @Override
            public void onSuccess(FAQCategoryItem response) {
                if (response == null) {
                    callback.onError();
                } else {
                    callback.receive(response);
                    cache.insertCategory(id, response);
                }
            }
        });
    }

    @Override
    public void getCategoriesForApplication(final GetCallback<List<String>> callback) {
        accessChecker.checkAccess();

        if ((application == null) || (language == null) || (departmentKey == null)) {
            callback.onError();
            return;
        }

        client.getActions().getCategoriesForApplication(application, language, departmentKey,
                new DefaultCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> response) {
                callback.receive(response);
            }
        });
    }

    @Override
    public void getCachedCategory(String id, GetCallback<FAQCategory> callback) {
        accessChecker.checkAccess();

        cache.getCategory(id, callback);
    }

    @Override
    public void getItem(String id, @Nullable FAQItemSource openFrom, final GetCallback<FAQItem> callback) {
        accessChecker.checkAccess();

        if (openFrom != null) {
            client.getActions().track(id, openFrom);
        }

        client.getActions().getItem(id, deviceId, new DefaultCallback<FAQItemItem>() {
            @Override
            public void onSuccess(FAQItemItem response) {
                if (response == null) {
                    callback.onError();
                } else {
                    callback.receive(response);
                }
            }
        });
    }

    @Override
    public void getCachedItem(String id, @Nullable FAQItemSource openFrom, GetCallback<FAQItem> callback) {
        accessChecker.checkAccess();

        if (openFrom != null) {
            client.getActions().track(id, openFrom);
        }
        cache.getItem(id, callback);
    }

    @Override
    public void search(
            String query,
            String categoryId,
            int limit,
            final GetCallback<List<FAQSearchItem>> callback) {
        accessChecker.checkAccess();

        client.getActions().getSearch(
                query,
                categoryId,
                limit,
                new DefaultCallback<List<FAQSearchItemItem>>() {
            @Override
            public void onSuccess(List<FAQSearchItemItem> response) {
                if (response == null) {
                    callback.onError();
                } else {
                    List<FAQSearchItem> items = new ArrayList<>();
                    items.addAll(response);
                    callback.receive(items);
                }
            }
        });
    }

    @Override
    public void like(final FAQItem item, final GetCallback<FAQItem> callback) {
        accessChecker.checkAccess();
        client.getActions().like(item.getId(), deviceId, new DefaultCallback<DefaultResponse>() {
            @Override
            public void onSuccess(DefaultResponse response) {
                callback.receive(new FAQItemItem(item, FAQItem.UserRate.LIKE));
            }
        });
    }

    @Override
    public void dislike(final FAQItem item, final GetCallback<FAQItem> callback) {
        accessChecker.checkAccess();
        client.getActions().dislike(item.getId(), deviceId, new DefaultCallback<DefaultResponse>() {
            @Override
            public void onSuccess(DefaultResponse response) {
                callback.receive(new FAQItemItem(item, FAQItem.UserRate.DISLIKE));
            }
        });
    }

    private static class FAQDestroyer {
        @NonNull
        private final List<Runnable> actions = new ArrayList<>();
        private boolean destroyed;

        public FAQDestroyer() {
        }

        public void addDestroyAction(Runnable action) {
            actions.add(action);
        }

        public boolean isDestroyed() {
            return destroyed;
        }

        public void destroy() {
            if (!destroyed) {
                destroyed = true;
                for (Runnable action : actions) {
                    action.run();
                }
            }
        }
    }

    private static class AccessCheckerImpl implements AccessChecker {

        @NonNull
        private final Thread thread;
        @NonNull
        private final FAQDestroyer destroyer;

        public AccessCheckerImpl(@NonNull Thread thread, @NonNull FAQDestroyer destroyer) {
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
        private final FAQDestroyer destroyed;
        private final Handler handled;

        private ExecIfNotDestroyedHandlerExecutor(FAQDestroyer destroyed, Handler handled) {
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

    private static class DestroyIfNotErrorListener implements InternalErrorListener {
        @Nullable
        private final FAQDestroyer destroyer;
        @Nullable
        private final InternalErrorListener errorListener;

        private DestroyIfNotErrorListener(@Nullable FAQDestroyer destroyer,
                                          @Nullable InternalErrorListener errorListener) {
            this.destroyer = destroyer;
            this.errorListener = errorListener;
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
        }
    }
}
