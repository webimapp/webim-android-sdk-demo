package ru.webim.android.demo.client.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import ru.webim.android.demo.client.R;
import ru.webim.android.demo.client.WebimChatFragment;
import ru.webim.android.sdk.Message;

import static ru.webim.android.sdk.Message.Type.FILE_FROM_OPERATOR;
import static ru.webim.android.sdk.Message.Type.FILE_FROM_VISITOR;

public class MenuController {
    private final DynamicContextDialog dynamicContextDialog;
    private final RelativeLayout menuReplyLayout;
    private final RelativeLayout menuCopyLayout;
    private final RelativeLayout menuEditLayout;
    private final RelativeLayout menuDeleteLayout;
    private final WebimChatFragment webimChatFragment;
    private String contextMenuMessageId;

    public MenuController(WebimChatFragment webimChatFragment) {
        this.webimChatFragment = webimChatFragment;
        dynamicContextDialog = new DynamicContextDialog(webimChatFragment.getContext(), webimChatFragment.getActivity());
        dynamicContextDialog.setDimAmount(0.6f);
        dynamicContextDialog.setOnShadowAreaClickListener(view -> dismissContextDialog());
        View contextMenu = dynamicContextDialog.setContextMenuRes(R.layout.dialog_message_menu);

        menuReplyLayout = contextMenu.findViewById(R.id.relLayoutReply);
        menuCopyLayout = contextMenu.findViewById(R.id.relLayoutCopy);
        menuEditLayout = contextMenu.findViewById(R.id.relLayoutEdit);
        menuDeleteLayout = contextMenu.findViewById(R.id.relLayoutDelete);
    }

    public void updateSentMessageDialog(final Message message, final int adapterPosition) {
       updateMenuItems(message, adapterPosition, true);
    }

    public void updateReceivedMessageDialog(final Message message, final int adapterPosition) {
        updateMenuItems(message, adapterPosition, false);
    }

    private void updateMenuItems(@NonNull final Message message,
                                 final int adapterPosition,
                                 boolean isSentMessage) {
        if (!message.getClientSideId().toString().equals(contextMenuMessageId)) {
            return;
        }
        disableContextMenuItem(menuReplyLayout);
        disableContextMenuItem(menuCopyLayout);
        disableContextMenuItem(menuEditLayout);
        disableContextMenuItem(menuDeleteLayout);

        Message.Type messageFileType = isSentMessage ? FILE_FROM_VISITOR : FILE_FROM_OPERATOR;

        if (isSentMessage && message.getType() != messageFileType && message.getServerSideId() == null) {
            setCopyItemListener(message);
            return;
        }

        if (message.canBeReplied()) {
            enableContextMenuItem(menuReplyLayout);
            menuReplyLayout.setOnClickListener(view -> {
                webimChatFragment.onReplyMessageAction(message, adapterPosition);
                dismissContextDialog();
            });
        }

        if (message.getType() != messageFileType) {
            setCopyItemListener(message);

            if (isSentMessage && message.canBeEdited()) {
                enableContextMenuItem(menuEditLayout);
                menuEditLayout.setOnClickListener(view -> {
                    webimChatFragment.onEditMessageAction(message, adapterPosition);
                    dismissContextDialog();
                });
            }
        }

        if (isSentMessage && message.canBeEdited()) {
            enableContextMenuItem(menuDeleteLayout);
            menuDeleteLayout.setOnClickListener(view -> {
                webimChatFragment.onDeleteMessageAction(message);
                dismissContextDialog();
            });
        }
    }

    private void setCopyItemListener(@NonNull Message message) {
        enableContextMenuItem(menuCopyLayout);
        menuCopyLayout.setOnClickListener(view -> {
            ClipData clip = ClipData.newPlainText("message", message.getText());
            ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                showToast(view.getResources().getString(R.string.copied_message));
            } else {
                showToast(view.getResources().getString(R.string.copy_failed));
            }
            dismissContextDialog();
        });
    }

    private void disableContextMenuItem(ViewGroup menuItem) {
        menuItem.setVisibility(View.GONE);
    }

    private void enableContextMenuItem(ViewGroup menuItem) {
        menuItem.setVisibility(View.VISIBLE);
    }

    public void setContextMenuMessageId(String menuMessageId) {
        contextMenuMessageId = menuMessageId;
    }

    public void openContextDialog(View view, DynamicContextDialog.ConfigurationChangedListener listener) {
        if (enableContextDialog()) {
            dynamicContextDialog.setVisibleView(view);
            dynamicContextDialog.setConfigurationChangedListener(listener);
            view.post(new Runnable() {
                @Override
                public void run() {
                    dynamicContextDialog.show();
                }
            });
        }
    }

    public boolean closeContextDialog(String messageId) {
        if (dynamicContextDialog.isShowing() && contextMenuMessageId.equals(messageId)) {
            dismissContextDialog();
            return true;
        }
        return false;
    }

    private boolean enableContextDialog() {
        return menuReplyLayout.getVisibility() == View.VISIBLE ||
                menuCopyLayout.getVisibility() == View.VISIBLE;
    }

    private void dismissContextDialog() {
        contextMenuMessageId = null;
        dynamicContextDialog.dismiss();
    }

    private void showToast(String messageToast) {
        Toast.makeText(webimChatFragment.getContext(), messageToast, Toast.LENGTH_SHORT).show();
    }
}
