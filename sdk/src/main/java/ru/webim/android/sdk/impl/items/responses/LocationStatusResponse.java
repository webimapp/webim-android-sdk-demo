package ru.webim.android.sdk.impl.items.responses;

import com.google.gson.annotations.SerializedName;

public class LocationStatusResponse extends ErrorResponse {
    @SerializedName("onlineOperators")
    private boolean onlineOperators;
    @SerializedName("onlineStatus")
    private String onlineStatus;

    public LocationStatusResponse() {
    }

    public boolean operatorsOnline() {
        return onlineOperators;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }
}
