package ru.webim.android.sdk.impl.items;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Nikita Kaberov on 29.03.18.
 */

public class UnreadByVisitorMessagesItem {

    @SerializedName("msgCnt")
    private int msgCnt;
    @SerializedName("sinceTs")
    private double sinceTs;

    public UnreadByVisitorMessagesItem() {
        // Need for Gson No-args fix
    }

    public int getMessageCount() {
        return msgCnt;
    }

    public double getSinceTs() {
        return sinceTs;
    }
}
