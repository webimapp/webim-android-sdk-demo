package ru.webim.chatview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import ru.webim.android.sdk.Message;
import ru.webim.chatview.holders.BotMessageHolder;
import ru.webim.chatview.holders.MessageHolder;
import ru.webim.chatview.holders.ReceivedMessageHolder;
import ru.webim.chatview.holders.SentMessageHolder;

public class ChatAdapterImpl extends ChatAdapter<MessageHolder> {
    private MessageHolder.ChatHolderActions holderActions;

    public ChatAdapterImpl(MessageHolder.ChatHolderActions holderActions) {
        this.holderActions = holderActions;
    }

    @Override
    public MessageHolder onCreateMessageHolder(@NonNull ViewGroup parent, @NonNull ViewType vt) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getLayout(vt), parent, false);
        switch (vt) {
            case OPERATOR:
            case FILE_FROM_OPERATOR:
                return new ReceivedMessageHolder(view, holderActions);
            case VISITOR:
            case FILE_FROM_VISITOR:
                return new SentMessageHolder(view, holderActions);
            case INFO:
            case INFO_OPERATOR_BUSY:
                return new MessageHolder(view, holderActions);
            case KEYBOARD:
                return new BotMessageHolder(view, holderActions);
            default:
                return null;
        }
    }

    private int getLayout(ViewType viewType) {
        switch (viewType) {
            case OPERATOR:
            case FILE_FROM_OPERATOR:
                return R.layout.item_message_received;
            case VISITOR:
            case FILE_FROM_VISITOR:
                return R.layout.item_message_sent;
            case INFO_OPERATOR_BUSY:
                return R.layout.item_op_busy_message;
            case KEYBOARD:
                return R.layout.item_keyboard_message;
            case KEYBOARD_RESPONSE:
            case INFO:
            default:
                return R.layout.item_info_message;
        }
    }

    @Override
    public void onBindMessageHolder(@NonNull MessageHolder holder, @NonNull Message message, int position) {
        Message prev = position < messageList.size() - 1 ? messageList.get(messageList.size() - position - 2) : null;
        holder.bind(message, false);
    }
}
