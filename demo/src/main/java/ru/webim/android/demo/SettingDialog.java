package ru.webim.android.demo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.util.regex.Pattern;

import ru.webim.android.demo.util.SettingDialogCallback;

public class SettingDialog extends DialogFragment {

    private final String dialogTitle;
    private final String dialogHint;
    private final String dialogValue;
    private final SettingDialogCallback settingDialogCallback;
    private EditText editText;
    private TextView errorMessage;
    private boolean fieldUrl;

    public SettingDialog(String dialogTitle,
                         String dialogHint,
                         String dialogValue,
                         boolean fieldUrl,
                         SettingDialogCallback settingDialogCallback) {
        this.dialogTitle = dialogTitle;
        this.dialogHint = dialogHint;
        this.dialogValue = dialogValue;
        this.fieldUrl = fieldUrl;
        this.settingDialogCallback = settingDialogCallback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_setting, null);
        editText = view.findViewById(R.id.editSettingDialog);
        errorMessage = view.findViewById(R.id.errorMessage);
        TextView hintText = view.findViewById(R.id.hintSettingDialog);
        if (dialogHint == null) {
            hintText.setVisibility(View.GONE);
        } else {
            hintText.setVisibility(View.VISIBLE);
            hintText.setText(dialogHint);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(dialogTitle)
                .setView(view)
                .setPositiveButton(
                        getResources().getString(R.string.dialog_ok),
                        (dialog, which) -> {
                            String newValue = editText.getText().toString().trim();
                            if (!dialogValue.equalsIgnoreCase(newValue)) {
                                settingDialogCallback.onNewValue(newValue);
                            }
                            dialog.dismiss();
                        })
                .setNegativeButton(
                        getResources().getString(R.string.dialog_cancel),
                        (dialog, which) -> dialog.dismiss());
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog alertDialog = (AlertDialog) getDialog();
        if (alertDialog != null) {
            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            editText.setText(dialogValue);
            editText.setSelection(dialogValue.length());
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String text = editable.toString().trim();

                    errorMessage.setVisibility(View.INVISIBLE);
                    positiveButton.setEnabled(true);
                    int colorEditTint = ContextCompat.getColor(requireContext(), R.color.webim_color_accent);

                    if (!isUrlValid(text)) {
                        if (text.length() > 0) {
                            errorMessage.setVisibility(View.VISIBLE);
                            colorEditTint = ContextCompat.getColor(requireContext(), R.color.red);
                        }
                        positiveButton.setEnabled(false);
                    }
                    editText.setBackgroundTintList(ColorStateList.valueOf(colorEditTint));
                }
            });
        }
    }

    private boolean isUrlValid(String url) {
        if (fieldUrl && url.contains("://")) {
            return Patterns.WEB_URL.matcher(url).matches();
        }
        return Pattern.compile("^\\w+$").matcher(url).matches();
    }
}
