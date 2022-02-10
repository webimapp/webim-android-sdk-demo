package ru.webim.android.sdk.impl.backend;


import android.content.SharedPreferences;

import ru.webim.android.sdk.MessageStream;


/**
 * Created by Nikita Lazarev-Zubov on 03.08.17.
 */


public class LocationSettingsImpl implements MessageStream.LocationSettings {

    // Key names for storing properties in SharedPreferences.
    private static final String PREFS_KEY_HINTS_ENABLED = "hints_enabled";

    private final boolean hintsEnabled;


    public LocationSettingsImpl(boolean hintsEnabled) {
        this.hintsEnabled = hintsEnabled;
    }


    // Get from and save to SharedPreferences methods.

    public static LocationSettingsImpl getConfigFromPreferences(SharedPreferences preferences) {
        return new LocationSettingsImpl(preferences.getBoolean(PREFS_KEY_HINTS_ENABLED, false));
    }

    public void saveLocationSettingsToPreferences(SharedPreferences preferences) {
        preferences.edit().putBoolean(PREFS_KEY_HINTS_ENABLED, hintsEnabled).apply();
    }


    // MessagesStream.LocationSettings interface methods.

    @Override
    public boolean areHintsEnabled() {
        return hintsEnabled;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if ((object == null) ||
                (getClass() != object.getClass())) {
            return false;
        }

        LocationSettingsImpl locationSettings = (LocationSettingsImpl) object;

        return hintsEnabled == locationSettings.hintsEnabled;
    }

    @Override
    public int hashCode() {
        return (hintsEnabled ? 1 : 0);
    }

}