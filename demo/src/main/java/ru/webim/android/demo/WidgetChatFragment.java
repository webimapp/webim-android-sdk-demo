package ru.webim.android.demo;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import ru.webim.android.sdk.Operator;
import ru.webim.android.sdk.WebimSession;
import ru.webim.chatview.ui.ChatView;
import ru.webim.chatview.utils.FileHelper;

public class WidgetChatFragment extends Fragment {
    private WebimSession session;
    private ChatView chatView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_widget_chat, container, false);

        chatView = rootView.findViewById(R.id.chatView);
        chatView.setSession(session);

        setPreChatActions();
        setupToolbar(rootView);

        return rootView;
    }

    public void setWebimSession(WebimSession session) {
        if (this.session != null) {
            throw new IllegalStateException("Webim session is already set");
        }
        this.session = session;
    }

    private void setPreChatActions() {
        Intent intent = requireActivity().getIntent();
        Bundle args = intent.getExtras();
        String type = intent.getType();
        String action = intent.getAction();

        if (args != null) {
            if (args.getBoolean(WebimChatActivity.EXTRA_SHOW_RATING_BAR_ON_STARTUP, false)) {
                chatView.addMessagesSyncedCallback(() -> chatView.getChatEditBar().showRatingDialog());
            }
        }

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent);
            } else {
                handleSendFile(intent);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            handleSendMultipleFiles(intent);
        }
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            chatView.addMessagesSyncedCallback(() -> session.getStream().sendMessage(sharedText));
        }
    }

    void handleSendFile(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            FileHelper fileHelper = chatView.getFileHelper();
            chatView.addMessagesSyncedCallback(() -> fileHelper.sendFileWithNewTemp(imageUri));
        }
    }

    private void handleSendMultipleFiles(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            FileHelper fileHelper = chatView.getFileHelper();
            for (Uri uri : imageUris) {
                chatView.addMessagesSyncedCallback(() -> fileHelper.sendFileWithNewTemp(uri));
            }
        }
    }

    private void setupToolbar(final View rootView) {
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(rootView.findViewById(R.id.toolbar));
            final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
            }
        }

        final TextView typingView = rootView.findViewById(R.id.action_bar_subtitle);
        Operator operator = session.getStream().getCurrentOperator();
        setOperatorName(typingView, operator);
        setOperatorAvatar(rootView, operator);

        session.getStream().setOperatorTypingListener(isTyping -> {
            ImageView imageView = rootView.findViewById(R.id.image_typing);
            imageView.setBackgroundResource(R.drawable.typing_animation);
            AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();

            if (isTyping) {
                typingView.setText(getString(R.string.operator_typing));
                typingView.setTextColor(rootView.getResources().getColor(R.color.colorTexWhenTyping));
                imageView.setVisibility(View.VISIBLE);
                animationDrawable.start();
            } else {
                setOperatorName(typingView, session.getStream().getCurrentOperator());
                typingView.setTextColor(rootView.getResources().getColor(R.color.white));
                imageView.setVisibility(View.GONE);
                animationDrawable.stop();
            }
        });
        session.getStream().setCurrentOperatorChangeListener((oldOperator, newOperator) -> {
            setOperatorAvatar(rootView, newOperator);
            setOperatorName(typingView, session.getStream().getCurrentOperator());
        });
    }

    private void setOperatorName(TextView textView, Operator operator) {
        String operatorName = operator == null
            ? getString(R.string.no_operator)
            : operator.getName();
        textView.setText(getString(R.string.operator_name, operatorName));
    }

    protected void setOperatorAvatar(View view, @Nullable Operator operator) {
        if (operator != null) {
            if (operator.getAvatarUrl() != null) {
                Glide.with(requireActivity())
                    .load(operator.getAvatarUrl())
                    .into((ImageView) view.findViewById(R.id.sender_photo));
            } else {
                if (getContext() != null) {
                    ((ImageView) view.findViewById(R.id.sender_photo)).setImageDrawable(
                        requireActivity().getResources().getDrawable(R.drawable.default_operator_avatar));
                }
            }
            view.findViewById(R.id.sender_photo).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.sender_photo).setVisibility(View.GONE);
        }
    }

    public void onBackPressed() {
    }
}
