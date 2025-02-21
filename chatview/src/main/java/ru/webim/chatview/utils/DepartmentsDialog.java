package ru.webim.chatview.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.List;

import ru.webim.chatview.R;

public class DepartmentsDialog extends DialogFragment {

    public static final String DIALOG_TAG = DepartmentsDialog.class.getSimpleName();
    private List<String> departmentNames;
    private final DepartmentItemSelectedCallback listener;

    public DepartmentsDialog(DepartmentItemSelectedCallback listener) {
        this.listener = listener;
    }

    public void setDepartmentNames(List<String> departmentNames) {
        this.departmentNames = departmentNames;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.choose_department)
            .setItems(departmentNames.toArray(new String[0]), (dialog, which) -> listener.itemSelected(which))
            .setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    listener.onBackPressed();
                    dialog.dismiss();
                }
                return true;
            });
        return builder.create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        listener.onDismissed();
    }

    public interface DepartmentItemSelectedCallback {

        void itemSelected(int position);

        void onBackPressed();

        void onDismissed();
    }
}
