package ru.webim.android.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * MessageTracker has two purposes:
 * <ol>
 *     <li>Allows to request the messages which are above in history</li>
 *     <li>Defines an interval within which message changes are transmitted to the listener (See {@link MessageStream#newMessageTracker})</li>
 * </ol>
 * @see MessageStream#newMessageTracker
 */
public interface MessageTracker {

    /**
     * Requests the messages above in history. Returns not more than 'limitOfMessages' of messaged.
     * If an empty list is returned, it indicated the end of the message history.
     * Notice that this method can not be called again until the callback for the previous call will
     * be invoked.
     * @param limitOfMessages will be returned not more than the specified number of messages
     * @param callback a callback
     * @throws IllegalArgumentException if limit <= 0
     * @throws IllegalStateException if the previous request was not completed
     * @throws IllegalStateException if the MessageTracker or the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    void getNextMessages(int limitOfMessages, @NonNull GetMessagesCallback callback);

    /**
     * Requests the messages from history. Returns not more than 'limitOfMessages' of messaged.
     * If an empty list is returned, it indicated there no messages in history yet.
     * Notice that this method can not be called again until the callback for the previous call will
     * be invoked.
     * @param limitOfMessages will be returned not more than the specified number of messages
     * @param callback a callback
     * @throws IllegalArgumentException if limit <= 0
     * @throws IllegalStateException if the previous request was not completed
     * @throws IllegalStateException if the MessageTracker or the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    void getLastMessages(int limitOfMessages, GetMessagesCallback callback);

    /**
     * Requests all messages from history. If an empty list is passed inside completion, there no
     * messages in history yet.
     * If there is any previous {@link MessageTracker} request that is not completed, or current
     * {@link MessageTracker} has been destroyed, this method will do nothing.
     * This method is totally independent on {@link MessageTracker#getLastMessages} and
     * {@link MessageTracker#getLastMessages} methods' calls.
     * @param callback a callback
     * @throws IllegalStateException if the MessageTracker or the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    void getAllMessages(GetMessagesCallback callback);

    /**
     * Method sets listener that is called when messages syncs with server
     * @param messagesSyncedListener listener that will be called {@link MessagesSyncedListener}
     */
    void setMessagesSyncedListener(@Nullable MessagesSyncedListener messagesSyncedListener);

    /**
     * This method loads all history since certain message.
     * @param sinceMessage message the history will be received from
     * @param messagesCallback callback that will be called
     * */
    void loadAllHistorySince(@NonNull Message sinceMessage, @NonNull GetMessagesCallback messagesCallback);

    /**
     * MessageTracker retains some range of messages. By using this method one can move the upper limit of this range
     * to another message.
     * Notice that this method can not be used unless the previous call was finished (was invoked callback) {@link MessageTracker#getNextMessages}
     * @param message message reset to
     * @throws IllegalStateException if the previous {@link MessageTracker#getNextMessages} request was not completed
     * @throws IllegalStateException if the MessageTracker or the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was created in
     */
    void resetTo(@NonNull Message message);

    /**
     * Destroys the MessageTracker. It is impossible to use any MessageTracker methods after it was destroyed.
     * @throws IllegalStateException if the MessageTracker or the WebimSession was destroyed
     * @throws RuntimeException if the method called not from the thread on which the WebimSession was created
     */
    void destroy();

    /**
     * @see MessageTracker#getNextMessages
     */
    interface GetMessagesCallback {
        void receive(@NonNull List<? extends Message> messages);
    }

    /**
     * Listener that is called when messages syncs with server
     */
    interface MessagesSyncedListener {
        void messagesSynced();
    }
}
