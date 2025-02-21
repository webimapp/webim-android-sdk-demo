package ru.webim.android.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ErrorFragment extends Fragment {
    private static final String EXTRA_HEADER = "extra_header";
    private static final String EXTRA_DESC = "extra_desc";
    private static final String EXTRA_ARGS = "extra_args";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_error, container, false);
        Bundle args = getArguments();
        ((TextView) v.findViewById(R.id.errorHeader)).setText(args.getInt(EXTRA_HEADER));
        ((TextView) v.findViewById(R.id.errorDesc)).setText(
            getResources().getString(args.getInt(EXTRA_DESC),
            (Object[]) args.getStringArray(EXTRA_ARGS))
        );
        return v;
    }

    public static ErrorFragment newInstance(int errorHeaderId,
                                            int errorDescId,
                                            String... formatArgs) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_HEADER, errorHeaderId);
        args.putInt(EXTRA_DESC, errorDescId);
        args.putStringArray(EXTRA_ARGS, formatArgs);
        ErrorFragment fragment = new ErrorFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
