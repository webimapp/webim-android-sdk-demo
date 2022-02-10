package ru.webim.android.sdk.impl.items.responses;

import com.google.gson.annotations.SerializedName;

public class ErrorResponse {
    @SerializedName("argumentName")
    private String argumentName;
    @SerializedName("error")
    private String error;

    public ErrorResponse() {

    }

    public String getError() {
        return error;
    }

    public String getArgumentName() {
        return argumentName;
    }
}
