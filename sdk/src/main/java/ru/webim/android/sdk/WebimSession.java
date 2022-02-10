package ru.webim.android.sdk;

import androidx.annotation.NonNull;

public interface WebimSession {

    /**
     * Resumes session networking.
     * Notice that a session is created as a paused, i.e. to start using it
     * the first thing to do is to call this method
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was created in
     */
    void resume();

    /**
     * Pauses session networking.
     * @throws RuntimeException if the method was called not from the thread the WebimSession was created in
     */
    void pause();

    /**
     * Destroys the session. It is impossible to use any session methods after it was destroyed.
     * @throws RuntimeException if the method was called not from the thread the WebimSession was created in
     */
    void destroy();

    /**
     * Destroys the session, performing a cleanup. It is impossible to use any session methods after it was destroyed.
     * @throws RuntimeException if the method was called not from the thread the WebimSession was created in
     */
    void destroyWithClearVisitorData();

    /**
     * @return a {@link MessageStream} object, attached to this session. Each invocation of this method returns the same object
     */
    @NonNull MessageStream getStream();

    /**
     * Changes location without creating a new session.
     * @param location New location name.
     * */
    void changeLocation(@NonNull String location);

    /**
     * This method allows you to manually set the push token. Notice that in most cases you should NOT use this method: push token
     * retrieves automatically if the push system is enabled. This method may be useful for debugging.
     * @param pushToken push token
     * @throws IllegalStateException if the push system was not configured for this session by {@link Webim.SessionBuilder#setPushSystem}
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was created in
     * @see Webim.SessionBuilder#setPushToken
     */
    void setPushToken(@NonNull String pushToken);

    /**
     * This method allows you to manually unbind the push token.
     * @param tokenCallback - tokenCallback shows if a call is completed or failed.
     */
    void removePushToken(@NonNull TokenCallback tokenCallback);

    /**
     * @see WebimSession#removePushToken(TokenCallback tokenCallback)
     */
    interface TokenCallback {
        /**
         * Invoked if request succeed.
         */
        void onSuccess();

        /**
         * Invoked if request failed.
         * @param webimError - error that was occurred
         * @see TokenError
         */
        void onFailure(WebimError<TokenError> webimError);

        /**
         * @see TokenCallback#onFailure
         */
        enum TokenError {
            /**
             * Timeout of unbinding push token was expired
             */
            SOCKET_TIMEOUT_EXPIRED,

            /**
             * An unexpected error occurred
             */
            UNKNOWN
        }
    }

    /**
     *  @see Webim.SessionBuilder#build(SessionCallback)
     */
    interface SessionCallback {

        /**
         * Invoked when session was successfully created on server.
         */
        void onSuccess();

        /**
         * Invoked when an error occurred while creating session on server.
         * @param sessionError Error
         * @see SessionError
         */
        void onFailure(WebimError<SessionError> sessionError);

        /**
         * @see SessionCallback#onFailure
         */
        enum SessionError {
            /**
             * Error occurred while sending init-request to server.
             */
            REQUEST_ERROR,

            /**
             * Error occurred while session parameter has invalid value.
             */
            INVALID_PARAMETER_VALUE,

            /**
             * An unexpected error
             */
            UNKNOWN
        }
    }
}