package ru.webim.android.demo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import ru.webim.android.sdk.WebimSession;

public class MainFragment extends Fragment {
    private WebimSession session;
    private ProgressBar progressBar;
    private TextView numberOfBadge;
    private MainFragmentDelegate delegate;

    public interface MainFragmentDelegate {
        void onOpenChat();

        void onOpenSettings();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        delegate = (MainFragmentDelegate) context;
    }

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[] { Manifest.permission.POST_NOTIFICATIONS }, 0);
            }
        }

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        initNewChatButton(rootView);
        initViewForBadge(rootView);
        initSettingButton(rootView);
        initCounter(rootView);

        return rootView;
    }

    public void setWebimSession(WebimSession webimSession) {
        session = webimSession;
    }

    private void initNewChatButton(View rootView) {
        Button newChatButton = rootView.findViewById(R.id.buttonStartChat);
        newChatButton.setOnClickListener(view -> delegate.onOpenChat());
    }

    private void initViewForBadge(View rootView) {
        progressBar = rootView.findViewById(R.id.progressBar);
        numberOfBadge = rootView.findViewById(R.id.textNumberOfBadge);
    }

    private void initSettingButton(View rootView) {
        Button settingsButton = rootView.findViewById(R.id.buttonSettings);
        settingsButton.setOnClickListener(view -> delegate.onOpenSettings());
    }

    private void initCounter(View rootView) {
        numberOfBadge.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        session.getStream().setUnreadByVisitorMessageCountChangeListener(newMessageCount -> {
            if (newMessageCount > 0) {
                numberOfBadge.setText(String.valueOf(newMessageCount));
                numberOfBadge.setVisibility(View.VISIBLE);
            } else {
                numberOfBadge.setVisibility(View.GONE);
            }
            progressBar.setVisibility(View.GONE);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        session.resume();
    }

    @Override
    public void onStop() {
        super.onStop();
        session.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        session.destroy();
    }
}
