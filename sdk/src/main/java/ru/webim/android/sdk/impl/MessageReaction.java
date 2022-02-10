package ru.webim.android.sdk.impl;

import com.google.gson.annotations.SerializedName;

public enum MessageReaction {
    @SerializedName("dislike")
    DISLIKE("dislike"),
    @SerializedName("like")
    LIKE("like");

    String value;

    MessageReaction(String value) {
        this.value = value;
    }
}
