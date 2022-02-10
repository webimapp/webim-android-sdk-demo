package ru.webim.android.sdk.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MessageSending extends MessageImpl {
    public MessageSending(@NonNull String serverUrl,
                          @NonNull Id id,
                          @NonNull String senderName,
                          @NonNull Type type,
                          @NonNull String text,
                          long timeMicros,
                          @Nullable Quote quote,
                          @Nullable Sticker sticker,
                          @Nullable Attachment attachment) {
        super(serverUrl,
                id,
                null,
                null,
                null,
                senderName,
                type,
                text,
                timeMicros,
                null,
                null,
                false,
                null,
                false,
                false,
                false,
                false,
                quote,
                null,
                null,
                sticker,
                null,
                false,
                false);
    }

    @NonNull
    @Override
    public SendStatus getSendStatus() {
        return SendStatus.SENDING;
    }
}
