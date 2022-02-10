package ru.webim.android.demo.client;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import ru.webim.android.demo.client.R;

import ru.webim.android.demo.client.util.SettingDialogCallback;

public class SettingDialog extends DialogFragment {

    private final String dialogTitle;
    private final String dialogHint;
    private final String dialogValue;
    private final SettingDialogCallback settingDialogCallback;
    private EditText editText;

    public SettingDialog(String dialogTitle,
                         String dialogHint,
                         String dialogValue,
                         SettingDialogCallback settingDialogCallback) {
        this.dialogTitle = dialogTitle;
        this.dialogHint = dialogHint;
        this.dialogValue = dialogValue;
        this.settingDialogCallback = settingDialogCallback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_setting, null);
        editText = view.findViewById(R.id.editSettingDialog);
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
                    positiveButton.setEnabled(editable.toString().trim().length() > 0);
                }
            });
        }
    }
}
