package ru.webim.android.sdk.impl;


import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import ru.webim.android.sdk.impl.backend.LocationSettingsImpl;


public class LocationSettingsHolder {

    private final SharedPreferences sharedPreferences;

    private final LocationSettingsImpl locationSettings;


    // Constructor.
    public LocationSettingsHolder(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        this.locationSettings = LocationSettingsImpl.getConfigFromPreferences(sharedPreferences);
    }


    public LocationSettingsImpl getLocationSettings() {
        return locationSettings;
    }


    public boolean receiveLocationSettings(@NonNull LocationSettingsImpl locationSettings) {
        if (!locationSettings.equals(this.locationSettings)) {
            locationSettings.saveLocationSettingsToPreferences(sharedPreferences);
            return true;
        }

        return false;
    }

}