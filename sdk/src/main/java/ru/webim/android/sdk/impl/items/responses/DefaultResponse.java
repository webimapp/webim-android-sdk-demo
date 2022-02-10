package ru.webim.android.sdk.impl.items.responses;

import com.google.gson.annotations.SerializedName;

public class DefaultResponse extends ErrorResponse {
    @SerializedName("result")
    private String result;

    public DefaultResponse() {
    }

    public String getResult() {
        return result;
    }

}
