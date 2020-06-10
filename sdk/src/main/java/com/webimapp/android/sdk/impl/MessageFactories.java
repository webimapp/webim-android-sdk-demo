package com.webimapp.android.sdk.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.webimapp.android.sdk.Message;
import com.webimapp.android.sdk.Operator;
import com.webimapp.android.sdk.impl.backend.WebimClient;
import com.webimapp.android.sdk.impl.items.KeyboardItem;
import com.webimapp.android.sdk.impl.items.KeyboardRequestItem;
import com.webimapp.android.sdk.impl.items.MessageItem;
import com.webimapp.android.sdk.impl.items.OperatorItem;
import com.webimapp.android.sdk.impl.items.StickerItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class MessageFactories {

    // may return null for incorrect or not supported message
    @Nullable
    private static MessageImpl fromWMMessage(@NonNull String serverUrl,
                                             boolean isHistoryMessage,
                                             @NonNull MessageItem message,
                                             @NonNull WebimClient client) {
        MessageItem.WMMessageKind kind = message.getType();

        if ((((kind == null)
                || (kind == MessageItem.WMMessageKind.CONTACTS))
                || (kind == MessageItem.WMMessageKind.FOR_OPERATOR))) {
            return null;
        }

        String text;
        Message.Attachment attachment = null;

        if ((kind == MessageItem.WMMessageKind.FILE_FROM_VISITOR)
                || (kind == MessageItem.WMMessageKind.FILE_FROM_OPERATOR)) {
            attachment = InternalUtils.getAttachment(serverUrl, message, client);
            if (attachment != null ) {
                text = attachment.getFileInfo().getFileName();
            } else {
                text = "";
            }
        } else {
            text = (message.getMessage() == null) ? "" : message.getMessage();
        }

        Message.Quote quote = InternalUtils.getQuote(serverUrl, message.getQuote(), client);

        Object json;
        if (kind == MessageItem.WMMessageKind.FILE_FROM_VISITOR || kind == MessageItem.WMMessageKind.FILE_FROM_OPERATOR) {
            json = message;
        } else {
            if (quote != null) {
                json = message.getQuote();
            } else {
                json = message.getData();
            }
        }

        String rawText = (json == null)
                ? null
                : new Gson().toJson(json);

        Message.Keyboard keyboardButton = null;
        if (kind == MessageItem.WMMessageKind.KEYBOARD) {
            Type mapType = new TypeToken<KeyboardItem>(){}.getType();
            KeyboardItem keyboard = InternalUtils.getItem(rawText, isHistoryMessage, mapType);
            keyboardButton = InternalUtils.getKeyboardButton(keyboard);
        }

        Message.KeyboardRequest keyboardRequest = null;
        if (kind == MessageItem.WMMessageKind.KEYBOARD_RESPONCE) {
            Type mapType = new TypeToken<KeyboardRequestItem>(){}.getType();
            KeyboardRequestItem keyboard = InternalUtils.getItem(rawText, isHistoryMessage, mapType);
            keyboardRequest = InternalUtils.getKeyboardRequest(keyboard);
        }

        Message.Sticker sticker = null;
        if (kind == MessageItem.WMMessageKind.STICKER_VISITOR) {
            Type mapType = new TypeToken<StickerItem>(){}.getType();
            StickerItem stickerItem = InternalUtils.getItem(rawText, isHistoryMessage, mapType);
            sticker = InternalUtils.getSticker(stickerItem);
        }

        return new MessageImpl(
                serverUrl,
                StringId.forMessage(message.getClientSideId()),
                message.getSessionId(),
                InternalUtils.getOperatorId(message),
                message.getSenderAvatarUrl(),
                message.getSenderName(),
                InternalUtils.toPublicMessageType(kind),
                text,
                message.getTimeMicros(),
                message.getId(),
                rawText,
                isHistoryMessage,
                attachment,
                message.isRead(),
                message.canBeEdited(),
                message.canBeReplied(),
                message.isEdited(),
                quote,
                keyboardButton,
                keyboardRequest,
                sticker);
    }

    public interface Mapper<T extends MessageImpl> {
        @Nullable
        T map(MessageItem msg);

        @NonNull
        List<T> mapAll(@NonNull List<MessageItem> list);

        void setClient(@NonNull WebimClient client);
    }

    public static abstract class AbstractMapper<T extends MessageImpl> implements Mapper<T> {
        @NonNull
        protected final String serverUrl;
        protected WebimClient client;

        protected AbstractMapper(@NonNull String serverUrl, @Nullable WebimClient client) {
            this.serverUrl = serverUrl;
            this.client = client;
        }

        @NonNull
        @Override
        public List<T> mapAll(@NonNull List<MessageItem> list) {
            List<T> ret = new ArrayList<>(list.size());
            for (MessageItem item : list) {
                T msg = map(item);
                if (msg != null) {
                    ret.add(msg);
                }
            }
            return ret;
        }
    }

    public static class MapperHistory extends AbstractMapper<MessageImpl> {

        protected MapperHistory(@NonNull String serverUrl, @Nullable WebimClient client) {
            super(serverUrl, client);
        }

        @Nullable
        @Override
        public MessageImpl map(MessageItem msg) {
            return fromWMMessage(serverUrl, true, msg, client);
        }

        @Override
        public void setClient(@NonNull WebimClient client) {
            this.client = client;
        }
    }

    public static class MapperCurrentChat extends AbstractMapper<MessageImpl> {

        protected MapperCurrentChat(@NonNull String serverUrl, @Nullable WebimClient client) {
            super(serverUrl, client);
        }

        @Nullable
        @Override
        public MessageImpl map(MessageItem msg) {
            return fromWMMessage(serverUrl, false, msg, client);
        }

        @Override
        public void setClient(@NonNull WebimClient client) {
            this.client = client;
        }
    }

    public static class SendingFactory {
        private final String serverUrl;

        public SendingFactory(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public MessageSending createText(Message.Id id, String text) {
            return new MessageSending(
                    serverUrl,
                    id,
                    "",
                    Message.Type.VISITOR,
                    text,
                    System.currentTimeMillis() * 1000,
                    null,
                    null);
        }

        public MessageSending createTextWithQuote(Message.Id id,
                                                  String text,
                                                  Message.Type quoteType,
                                                  String quoteAuthor,
                                                  String quoteText) {
            MessageImpl.QuoteImpl quote = new MessageImpl.QuoteImpl(
                    null,
                    null,
                    null,
                    quoteType,
                    quoteAuthor,
                    Message.Quote.State.PENDING,
                    quoteText,
                    0);
            return new MessageSending(
                    serverUrl,
                    id,
                    "",
                    Message.Type.VISITOR,
                    text,
                    System.currentTimeMillis() * 1000,
                    quote,
                    null);
        }

        public MessageSending createFile(Message.Id id, String fileName) {
            return new MessageSending(
                    serverUrl,
                    id,
                    "",
                    Message.Type.FILE_FROM_VISITOR,
                    fileName,
                    System.currentTimeMillis() * 1000,
                    null,
                    null);
        }

        public MessageSending createSticker(Message.Id id, int stickerId) {
            MessageImpl.Sticker sticker = new MessageImpl.StickerImpl(stickerId);
            return new MessageSending(
                    serverUrl,
                    id,
                    "",
                    Message.Type.STICKER_VISITOR,
                    "",
                    System.currentTimeMillis() * 1000,
                    null,
                    sticker);
        }
    }

    public static class OperatorFactory {
        private final String serverUrl;

        public OperatorFactory(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        @Nullable
        public Operator createOperator(@Nullable OperatorItem operatorItem) {
            return operatorItem == null
                    ? null
                    : new OperatorImpl(
                            StringId.forOperator(operatorItem.getId()),
                            operatorItem.getFullname(),
                            operatorItem.getAvatar() == null
                                    ? null
                                    : serverUrl + operatorItem.getAvatar());
        }
    }
}
