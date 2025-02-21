package ru.webim.android.sdk.impl.items;

import com.google.gson.annotations.SerializedName;

public class AccountConfigItem {
    @SerializedName("visitor_hits_api_endpoint")
    private String hintsEndpoint;

    public String getHintsEndpoint() {
        return hintsEndpoint;
    }
}
