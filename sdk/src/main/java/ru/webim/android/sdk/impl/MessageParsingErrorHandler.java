package ru.webim.android.sdk.impl;

import androidx.annotation.Nullable;

public interface MessageParsingErrorHandler {

    /**
     * Method is invoked when its unable to parse message from server or local database
     * @param messageId message id of the message
     * @param rawMessageContent raw content of the message
     */
    void onMessageParsingError(@Nullable String messageId, @Nullable String rawMessageContent);
}
