package ru.webim.android.sdk.impl.items;

import com.google.gson.annotations.SerializedName;

public class KeyboardRequestItem {
    @SerializedName("button")
    private KeyboardItem.Button button;
    @SerializedName("request")
    private Request request;

    public KeyboardItem.Button getButton() {
        return button;
    }

    public Request getRequest() {
        return  request;
    }


    public static final class Request {
        @SerializedName("messageId")
        private String messageId;

        public String getMessageId() {
            return messageId;
        }
    }

}
