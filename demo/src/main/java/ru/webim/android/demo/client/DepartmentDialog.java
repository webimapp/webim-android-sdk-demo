package ru.webim.android.demo.client;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import ru.webim.android.demo.client.R;

import ru.webim.android.demo.client.util.DepartmentItemSelectedCallback;

import java.util.List;

public class DepartmentDialog extends DialogFragment {

    public static final String DEPARTMENT_DIALOG_TAG = "departmentDialog";
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
