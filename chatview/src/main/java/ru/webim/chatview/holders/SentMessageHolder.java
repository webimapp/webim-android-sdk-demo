package ru.webim.chatview.holders;

import android.view.View;
import android.widget.ProgressBar;

import ru.webim.android.sdk.Message;
import ru.webim.chatview.R;

public class SentMessageHolder extends FileMessageHolder {
    ProgressBar sendingProgress;

    public SentMessageHolder(final View itemView, ChatHolderActions holderActions) {
        super(itemView, holderActions);

        messageBackground = R.drawable.background_send_message;
        bubbleColor = R.color.sending_msg_bubble;
        selectedColor = R.color.sending_msg_select;

        sendingProgress = itemView.findViewById(R.id.sending_msg);
    }

    @Override
    public void bind(Message message, boolean showDate) {
        super.bind(message, showDate);

        boolean sending = message.getSendStatus() == Message.SendStatus.SENDING;
        if (messageTime != null) {
            messageTime.setVisibility(sending ? View.GONE : View.VISIBLE);
        }
        if (sendingProgress != null) {
            sendingProgress.setVisibility(sending ? View.VISIBLE : View.GONE);
        }
    }
}
