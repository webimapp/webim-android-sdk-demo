package ru.webim.android.sdk;

import androidx.annotation.NonNull;

/**
 * @see Webim.SessionBuilder#setNotFatalErrorHandler(FatalErrorHandler)
 */
public interface NotFatalErrorHandler {
    /**
     * This method is called when a fatal error occurs.
     * @param error
     */
    void onNotFatalError(@NonNull WebimError<NotFatalErrorType> error);

    enum NotFatalErrorType {

        /**
         * This error indicates no network connection.
         */
        NO_NETWORK_CONNECTION,

        /**
         * This error occurs when server is not available or another reason for SocketTimeoutException.
         */
        SOCKET_TIMEOUT_EXPIRED
    }
}
