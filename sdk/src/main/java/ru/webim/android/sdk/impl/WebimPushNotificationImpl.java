package ru.webim.android.sdk.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ru.webim.android.sdk.WebimPushNotification;

public class WebimPushNotificationImpl implements WebimPushNotification {
    @SerializedName("event")
    private String event;
    @SerializedName("type")
    private NotificationType type;
    @SerializedName("params")
    private List<String> params;
    @SerializedName("location")
    private String location;
    @SerializedName("unread_by_visitor_msg_cnt")
    private int unreadByVisitorMessageCount;

    @NonNull
    @Override
    public NotificationType getType() {
        return type;
    }

    @NonNull
    @Override
    public String getEvent() {
        return event;
    }

    @NonNull
    @Override
    public List<String> getParams() {
        return params;
    }

    @Nullable
    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public int getUnreadByVisitorMessagesCount() {
        return unreadByVisitorMessageCount;
    }
}
