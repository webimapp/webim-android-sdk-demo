package ru.webim.android.demo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import ru.webim.android.demo.util.DepartmentItemSelectedCallback;

import java.util.List;

public class DepartmentDialog extends DialogFragment {

    public static final String DIALOG_TAG = DepartmentDialog.class.getSimpleName();
    private List<String> departmentNames;
    private final DepartmentItemSelectedCallback callback;

    public DepartmentDialog(DepartmentItemSelectedCallback callback) {
        this.callback = callback;
    }

    public void setDepartmentNames(List<String> departmentNames) {
        this.departmentNames = departmentNames;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.choose_department)
            .setItems(departmentNames.toArray(new String[0]), (dialog, which) -> callback.departmentItemSelected(which))
            .setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    callback.onBackPressed();
                    dialog.dismiss();
                }
                return true;
            });
        return builder.create();
    }
}
