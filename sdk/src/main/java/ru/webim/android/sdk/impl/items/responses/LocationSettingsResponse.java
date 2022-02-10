package ru.webim.android.sdk.impl.items.responses;

import com.google.gson.annotations.SerializedName;

public class LocationSettingsResponse extends ErrorResponse {
    @SerializedName("locationSettings")
    private Object rawLocationSettings;

    public LocationSettingsResponse() {
    }

    public Object getRawLocationSettings() {
        return rawLocationSettings;
    }
}
