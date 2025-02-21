package ru.webim.chatview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import ru.webim.android.sdk.Message;

public abstract class ChatAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
    protected final List<Message> messageList = new ArrayList<>();

    public abstract T onCreateMessageHolder(@NonNull ViewGroup parent, @NonNull ViewType viewType);

    public abstract void onBindMessageHolder(@NonNull T holder, @NonNull Message message, int position);

    @NonNull
    @Override
    public final T onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewType messageType = ViewType.values()[viewType];
        return onCreateMessageHolder(parent, messageType);
    }

    @Override
    public final void onBindViewHolder(@NonNull T holder, int position) {
        Message message = messageList.get(messageList.size() - position - 1);
        onBindMessageHolder(holder, message, position);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(messageList.size() - position - 1);
        if (message.getSendStatus() == Message.SendStatus.SENDING) {
            return ViewType.VISITOR.ordinal();
        }
        switch (message.getType()) {
            case OPERATOR:
                return ViewType.OPERATOR.ordinal();
            case VISITOR:
                return ViewType.VISITOR.ordinal();
            case OPERATOR_BUSY:
                return ViewType.INFO_OPERATOR_BUSY.ordinal();
            case FILE_FROM_VISITOR:
                return message.getAttachment() == null || message.getAttachment().getFileInfo().getImageInfo() != null
                    ? ViewType.VISITOR.ordinal()
                    : ViewType.FILE_FROM_VISITOR.ordinal();
            case FILE_FROM_OPERATOR:
                return message.getAttachment() == null || message.getAttachment().getFileInfo().getImageInfo() != null
                    ? ViewType.OPERATOR.ordinal()
                    : ViewType.FILE_FROM_OPERATOR.ordinal();
            case KEYBOARD:
                return ViewType.KEYBOARD.ordinal();
            case INFO:
            default:
                return ViewType.INFO.ordinal();
        }
    }

    public boolean add(Message message) {
        return messageList.add(message);
    }

    public void add(int position, Message message) {
        int prevItemsCount = messageList.size();
        messageList.add(position, message);
        if (position == 0 && prevItemsCount != 0) {
            int itemsAdded = 1;
            invalidateLastItemDate(itemsAdded);
        }
    }

    public boolean addAll(int position, Collection<? extends Message> collection) {
        int prevItemsCount = messageList.size();
        boolean messagesAdded = messageList.addAll(position, collection);
        if (messagesAdded && position == 0 && prevItemsCount != 0) {
            int itemsAdded = collection.size();
            invalidateLastItemDate(itemsAdded);
        }
        return messagesAdded;
    }

    public void invalidateMessage(String messageId) {
        for (int i = 0; i < messageList.size(); i++) {
            Message message = messageList.get(i);
            if (Objects.equals(message.getServerSideId(), messageId)) {
                int lastIndex = messageList.size() - 1;
                notifyItemChanged(lastIndex - i);
                break;
            }
        }
    }

    private void invalidateLastItemDate(int newItemsCount) {
        int lastItemIndex = messageList.size() - 1;
        notifyItemChanged(lastItemIndex - newItemsCount);
    }

    public Message set(int i, Message message) {
        return messageList.set(i, message);
    }

    public Message remove(int i, String messageId) {
        return messageList.remove(i);
    }

    public void clear() {
        messageList.clear();
    }

    public int indexOf(Message message) {
        return messageList.indexOf(message);
    }

    public int indexOf(String messageId) {
        for (int i = 0; i < messageList.size(); i++) {
            Message message = messageList.get(i);
            if (messageId.equals(message.getServerSideId())) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(Message message) {
        return messageList.lastIndexOf(message);
    }
}
