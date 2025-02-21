package ru.webim.android.demo.fcm;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import android.os.Build;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import ru.webim.android.demo.MainActivity;
import ru.webim.android.demo.R;
import ru.webim.android.demo.WebimChatActivity;
import ru.webim.android.sdk.Webim;
import ru.webim.android.sdk.WebimPushNotification;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final int NOTIFICATION_ID = "Webim".hashCode(); // Change to any number you want

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            sendNotification(remoteMessage);
            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }

        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private void scheduleJob() {

    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param remoteMessage FCM message body received.
     */
    private void sendNotification(RemoteMessage remoteMessage) {
        onPushMessage(
            getApplicationContext(),
            Webim.parseFcmPushNotification(remoteMessage.getData().toString())
        );
    }

    private static void onPushMessage(Context context,
                                      @Nullable WebimPushNotification push) {
        if (push == null) {
            return;
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String event = push.getEvent();
        if (event.equals("add")) {
            String message = getMessageFromPush(context, push);
            if (message == null) {
                return;
            }

            if (WebimChatActivity.isActive()) {
                return;
            }

            generateNotification(context, message, notificationManager, MainActivity.class, push.getType());
        } else if (event.equals("del")) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    @Nullable
    private static String getMessageFromPush(@NonNull Context context,
                                             @NonNull WebimPushNotification push) {
        try {
            String format;
            switch (push.getType()) {
                case CONTACT_INFORMATION_REQUEST:
                    format = context.getResources().getString(R.string.push_contact_information);
                    break;
                case OPERATOR_ACCEPTED:
                    format = context.getResources().getString(R.string.push_operator_accepted);
                    break;
                case OPERATOR_FILE:
                    format = context.getResources().getString(R.string.push_operator_file);
                    break;
                case OPERATOR_MESSAGE:
                    format = context.getResources().getString(R.string.push_operator_message);
                    break;
                case RATE_OPERATOR:
                    format = context.getResources().getString(R.string.push_rate_operator);
                    break;
                default:
                    return null;
            }

            return String.format(format, push.getParams().toArray());
        } catch (Exception ignore) {
        }

        return null;
    }

    private static void generateNotification(Context context,
                                             String message,
                                             NotificationManager notificationManager,
                                             Class<? extends Activity> cls,
                                             WebimPushNotification.NotificationType type) {
        String channelId = context.getResources().getString(R.string.channel_id);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence channelName = context.getResources().getString(R.string.channel_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationChannel.enableVibration(true);
            notificationChannel.setShowBadge(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        Resources res = context.getResources();
        if (cls != null) {
            Intent notificationIntent = new Intent(context, cls);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            notificationIntent.putExtra(MainActivity.EXTRA_NEED_OPEN_CHAT, true);
            notificationIntent.putExtra(WebimChatActivity.EXTRA_SHOW_RATING_BAR_ON_STARTUP, type == WebimPushNotification.NotificationType.RATE_OPERATOR);
            int flag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, flag);
            builder.setContentIntent(contentIntent);
        }
        builder.setSmallIcon(R.drawable.logo_webim_status_bar)
            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.default_operator_avatar))
            .setTicker(res.getString(R.string.app_name))
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.newmessageoperator));
        Notification notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
