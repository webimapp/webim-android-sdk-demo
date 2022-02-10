package ru.webim.android.demo.client;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initNewChatButton();
        initViewForBadge();
        initSettingButton();
        initCounter();
    }

    public void setWebimSession(WebimSession webimSession) {
        session = webimSession;
    }

    private void initNewChatButton() {
        Button newChatButton = requireView().findViewById(R.id.buttonStartChat);
        newChatButton.setOnClickListener(view -> delegate.onOpenChat());
    }

    private void initViewForBadge() {
        progressBar = requireView().findViewById(R.id.progressBar);
        numberOfBadge = requireView().findViewById(R.id.textNumberOfBadge);
    }

    private void initSettingButton() {
        Button settingsButton = requireView().findViewById(R.id.buttonSettings);
        settingsButton.setOnClickListener(view -> delegate.onOpenSettings());
    }

    private void initCounter() {
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
