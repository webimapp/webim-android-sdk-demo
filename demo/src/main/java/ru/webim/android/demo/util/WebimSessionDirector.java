package ru.webim.android.demo.util;

import static ru.webim.android.demo.SettingsFragment.KEY_ACCOUNT;
import static ru.webim.android.demo.SettingsFragment.KEY_FILE_LOGGER;
import static ru.webim.android.demo.SettingsFragment.KEY_LOCATION;
import static ru.webim.android.demo.SettingsFragment.KEY_NOTIFICATION;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

import ru.webim.android.demo.BuildConfig;
import ru.webim.android.sdk.Webim;
import ru.webim.android.sdk.WebimLog;
import ru.webim.android.sdk.WebimLogEntity;

public class WebimSessionDirector {

    public static void createSessionBuilderWithAnonymousVisitor(Context context, OnSessionBuilderCreatedListener listener) {
        Webim.SessionBuilder sessionBuilder = newSessionBuilder(context);
        retrieveFirebaseToken(context, sessionBuilder, listener);
    }

    public static void createSessionBuilderWithAuth1Visitor(Context context, OnSessionBuilderCreatedListener listener) {
        String visitorFieldAuthVersion1 =
            "{\"id\":\"1234567890987654321\"," +
                    "\"display_name\":\"Никита\"," +
                    "\"crc\":\"ffadeb6aa3c788200824e311b9aa44cb\"}";

        Webim.SessionBuilder sessionBuilder = newSessionBuilder(context).setVisitorFieldsJson(visitorFieldAuthVersion1);
        retrieveFirebaseToken(context, sessionBuilder, listener);
    }

    public static void createSessionBuilderWithAuth2Visitor(Context context, OnSessionBuilderCreatedListener listener) {
        String visitorFieldAuthVersion2 =
            "{\"fields\":{" +
                    "\"display_name\": \"Fedor\"," +
                    "\"email\": \"fedor@webim.ru\"," +
                    "\"phone\": \"123-3243\"," +
                    "\"id\": \"2113\"," +
                    "\"comment\": \"some\\ncomment\"," +
                    "\"info\": \"some\\ninfo\"," +
                    "\"icq\": \"12345678\"," +
                    "\"profile_url\": \"http:\\\\\\\\some-crm.ru\\\\id12345\"}," +
                    "\"expires\": 1605109076," +
                    "\"hash\": \"87233cd41af79e3736e73bfb12b803fb\"}";
        Webim.SessionBuilder sessionBuilder = newSessionBuilder(context).setVisitorFieldsJson(visitorFieldAuthVersion2);
        retrieveFirebaseToken(context, sessionBuilder, listener);
    }

    private static void retrieveFirebaseToken(Context context, Webim.SessionBuilder builder, OnSessionBuilderCreatedListener listener) {

        boolean hasFirebaseToken = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_NOTIFICATION, true);

        if (!hasFirebaseToken) {
            listener.onSessionBuilderCreated(builder);
            return;
        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                listener.onError(SessionBuilderError.FIREBASE_TOKEN_ERROR);
                return;
            }

            builder.setPushToken(task.getResult());
            listener.onSessionBuilderCreated(builder);
        });
    }

    private static Webim.SessionBuilder newSessionBuilder(Context context) {
        String DEFAULT_ACCOUNT_NAME = "demo";
        String DEFAULT_LOCATION = "mobile";
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        WebimLog webimLog = null;
        if (BuildConfig.DEBUG) {
            String webimLogTag = "webim_demo_log";
            if (sharedPref.getBoolean(KEY_FILE_LOGGER, false)) {
                webimLog = FileLogger.getAppSpecificDirLogger(context, webimLogTag + ".txt");
            } else {
                webimLog = log -> Log.i(webimLogTag, log);
            }
        }

        return Webim.newSessionBuilder()
            .setContext(context)
            .setAccountName(sharedPref.getString(KEY_ACCOUNT, DEFAULT_ACCOUNT_NAME))
            .setLocation(sharedPref.getString(KEY_LOCATION, DEFAULT_LOCATION))
            .setPushSystem(Webim.PushSystem.FCM)
            .setLoggerEntities(WebimLogEntity.SERVER)
            .setLogger(webimLog, Webim.SessionBuilder.WebimLogVerbosityLevel.VERBOSE);
    }

    public interface OnSessionBuilderCreatedListener {

        void onSessionBuilderCreated(Webim.SessionBuilder sessionBuilder);

        void onError(SessionBuilderError error);
    }

    public enum SessionBuilderError {
        FIREBASE_TOKEN_ERROR,
        AUTH_USER_ERROR
    }
}
