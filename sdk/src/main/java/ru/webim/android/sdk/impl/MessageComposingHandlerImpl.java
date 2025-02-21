package ru.webim.android.sdk.impl;

import android.os.Handler;
import androidx.annotation.Nullable;

import ru.webim.android.sdk.impl.backend.WebimActions;

public class MessageComposingHandlerImpl implements MessageComposingHandler {
    private static final int SEND_DRAFT_INTERVAL = 1000;
    private static final int RESET_STATUS_DELAY = 5000;

    private final Handler handler;
    private final WebimActions actions;

    private final Runnable resetTypingStatus = new Runnable() {
        @Override
        public void run() {
            actions.setVisitorTyping(false, null, false);
        }
    };

    private String latestDraft;
    private boolean isUpdateDraftScheduled;


    public MessageComposingHandlerImpl(Handler handler, WebimActions actions) {
        this.handler = handler;
        this.actions = actions;
    }

    public void setComposingMessage(final @Nullable String draftMessage) {
        this.latestDraft = draftMessage;

        if (!isUpdateDraftScheduled) {
            sendDraft(draftMessage);
            isUpdateDraftScheduled = true;
            handler.postDelayed(() -> {
                isUpdateDraftScheduled = false;
                if (!InternalUtils.equals(latestDraft, draftMessage)) {
                    sendDraft(latestDraft);
                }
            }, SEND_DRAFT_INTERVAL);
        }

        handler.removeCallbacks(resetTypingStatus);
        if (draftMessage != null) {
            handler.postDelayed(resetTypingStatus, RESET_STATUS_DELAY);
        }
    }

    private void sendDraft(String draftMessage) {
        actions.setVisitorTyping(draftMessage != null,
                draftMessage, draftMessage == null);
    }
}
