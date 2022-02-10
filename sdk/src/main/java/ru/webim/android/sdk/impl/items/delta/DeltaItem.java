package ru.webim.android.sdk.impl.items.delta;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class DeltaItem<T> {
    @SerializedName("objectType")
    protected Type objectType;
    @SerializedName("event")
    protected Event event;
    @SerializedName("id")
    protected String id;
    @SerializedName("data")
    protected T data;

    public DeltaItem() {
        // Need for Gson No-args fix
    }

    // null if unknown type
    public @Nullable Type getObjectType() {
        return objectType;
    }

    public @NonNull String getId() {
        return id;
    }

    public @NonNull Event getEvent() {
        return event;
    }

    public @NonNull T getData() {
        return data;
    }

    public enum Type {
        @SerializedName("CHAT")
        CHAT,
        @SerializedName("CHAT_MESSAGE")
        CHAT_MESSAGE,
        @SerializedName("CHAT_OPERATOR")
        CHAT_OPERATOR,
        @SerializedName("DEPARTMENT_LIST")
        DEPARTMENT_LIST,
        @SerializedName("HISTORY_REVISION")
        HISTORY_REVISION,
        @SerializedName("OPERATOR_RATE")
        OPERATOR_RATE,
        @SerializedName("CHAT_OPERATOR_TYPING")
        CHAT_OPERATOR_TYPING,
        @SerializedName("CHAT_READ_BY_VISITOR")
        CHAT_READ_BY_VISITOR,
        @SerializedName("CHAT_STATE")
        CHAT_STATE,
        @SerializedName("CHAT_UNREAD_BY_OPERATOR_SINCE_TS")
        CHAT_UNREAD_BY_OPERATOR_SINCE_TIMESTAMP,
        @SerializedName("OFFLINE_CHAT_MESSAGE")
        OFFLINE_CHAT_MESSAGE,
        @SerializedName("UNREAD_BY_VISITOR")
        UNREAD_BY_VISITOR,
        @SerializedName("VISIT_SESSION")
        VISIT_SESSION,
        @SerializedName("VISIT_SESSION_STATE")
        VISIT_SESSION_STATE,
        @SerializedName("MESSAGE_READ")
        READ_MESSAGE,
        @SerializedName("SURVEY")
        SURVEY
    }

    public enum Event {
        @SerializedName("add")
        ADD,
        @SerializedName("del")
        DELETE,
        @SerializedName("upd")
        UPDATE
    }
}
