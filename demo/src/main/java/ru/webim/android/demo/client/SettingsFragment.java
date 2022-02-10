package ru.webim.android.demo.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import ru.webim.android.demo.client.R;

import java.util.regex.Pattern;

public class SettingsFragment extends Fragment {
    private static final String KEY_ACCOUNT = "account";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_NOTIFICATION = "fcm";
    private static final String DEFAULT_ACCOUNT = "demo";
    private static final String DEFAULT_LOCATION = "mobile";
    private static final Uri LINK_PRIVACY = Uri.parse("https://webim.ru/privacy/");
    private SharedPreferences sharedPreferences;
    private TextView textAccount;
    private TextView textLocation;
    private SettingDialog settingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        initLayoutAccount(view);
        initLayoutLocation(view);
        initCheckBoxNotification(view);
        initTextPrivacy(view);
        initTextVersion(view);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        checkFieldsValidity();
    }

    private void initLayoutAccount(View rootView) {
        LinearLayout layoutAccount = rootView.findViewById(R.id.layoutAccount);
        layoutAccount.setOnClickListener(view -> {
            settingDialog = new SettingDialog(
                getString(R.string.prefs_account_title),
                getString(R.string.settings_account_field_hint),
                textAccount.getText().toString(),
                newAccount -> {
                    String verifiedValue = checkNewValueValidity(KEY_ACCOUNT, newAccount);
                    sharedPreferences.edit().putString(KEY_ACCOUNT, verifiedValue).apply();
                    textAccount.setText(verifiedValue);
                });
            settingDialog.show(getParentFragmentManager(), null);
        });

        textAccount = rootView.findViewById(R.id.textAccount);
        textAccount.setText(sharedPreferences.getString(KEY_ACCOUNT, DEFAULT_ACCOUNT));
    }

    private void initLayoutLocation(View rootView) {
        LinearLayout layoutLocation = rootView.findViewById(R.id.layoutLocation);
        layoutLocation.setOnClickListener(view -> {
            settingDialog = new SettingDialog(
                getString(R.string.prefs_location_title),
                null,
                textLocation.getText().toString(),
                newLocation -> {
                    String verifiedValue = checkNewValueValidity(KEY_LOCATION, newLocation);
                    sharedPreferences.edit().putString(KEY_LOCATION, verifiedValue).apply();
                    textLocation.setText(verifiedValue);
                });
            settingDialog.show(getParentFragmentManager(), null);
        });

        textLocation = rootView.findViewById(R.id.textLocation);
        textLocation.setText(sharedPreferences.getString(KEY_LOCATION, DEFAULT_LOCATION));
    }

    private void initCheckBoxNotification(View rootView) {
        CheckBox checkBoxNotification = rootView.findViewById(R.id.checkBoxNotification);
        checkBoxNotification.setChecked(sharedPreferences.getBoolean(KEY_NOTIFICATION, true));
        checkBoxNotification.setOnCheckedChangeListener((buttonView, isChecked) ->
                sharedPreferences.edit().putBoolean(KEY_NOTIFICATION, isChecked).apply());
    }

    private void initTextPrivacy(View rootView) {
        TextView textPrivacy = rootView.findViewById(R.id.textPrivacy);
        textPrivacy.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(LINK_PRIVACY);
            startActivity(browserIntent);
        });
    }

    private void initTextVersion(View rootView) {
        TextView textVersion = rootView.findViewById(R.id.textVersion);
        String appVersion = BuildConfig.VERSION_NAME;
        String sdkVersion = ru.webim.android.sdk.BuildConfig.VERSION_NAME;
        textVersion.setText(getString(R.string.prefs_version, appVersion, sdkVersion));
    }

    private void checkFieldsValidity() {
        if (isFieldInvalidate(KEY_ACCOUNT, textAccount.getText().toString())) {
            sharedPreferences.edit().putString(KEY_ACCOUNT, DEFAULT_ACCOUNT).apply();
            textAccount.setText(DEFAULT_ACCOUNT);
        }
        if (isFieldInvalidate(KEY_LOCATION, textLocation.getText().toString())) {
            sharedPreferences.edit().putString(KEY_ACCOUNT, DEFAULT_LOCATION).apply();
            textLocation.setText(DEFAULT_LOCATION);
        }
    }

    private String checkNewValueValidity(String key, String value) {
        if (key.equals(KEY_ACCOUNT) && isFieldInvalidate(KEY_ACCOUNT, value)) {
            return DEFAULT_ACCOUNT;
        } else if (key.equals(KEY_LOCATION) && isFieldInvalidate(KEY_LOCATION, value)) {
            return DEFAULT_LOCATION;
        } else {
            return value;
        }
    }

    private boolean isFieldInvalidate(String key, String url) {
        if (key.equals(KEY_ACCOUNT) && url.contains("://")) {
            return !Patterns.WEB_URL.matcher(url).matches();
        }
        return !Pattern.compile("^\\w+$").matcher(url).matches();
    }
}
