package ru.webim.android.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @see MessageStream#newMessageTracker(MessageListener)
 */
public interface MessageListener {

    /**
     * Called when adding a new message. If {@code before} == null, then it should be added to the end of message history 
	 * (the lowest message is added), in other cases the message should be inserted <b>before</b> the message (i.e. above it in history)
	 * which was given as a parameter {@code before}.
	 * Notice that this is a logical insertion of a message. I.e. calling this method does not necessarily mean receiving
	 * a new (unread) message. Moreover at the first call {@link MessageTracker#getNextMessages} most often the last messages of a <b>local</b> history  
	 * (i.e. which is stored on a user's device) are returned, and this method will be called for each message received from a server after a
	 * successful connection.
     * @param before a message, before which it is needed to implement an insert. If null then an insert is performed at the end of the list.
     * @param message added message
     */
    void messageAdded(@Nullable Message before, @NonNull Message message);

    /**
     * Called when removing a message.
     * @param message a message to be removed
     */
    void messageRemoved(@NonNull Message message);

    /**
	 * Called when changing a message. {@link Message} is an <b>immutable</b> type and field values can not be changed.
	 * That is why message changing occurs as replacing one object with another. Thereby you can find out, for example, which certain message 
	 * fields have changed by comparing an old and a new object values.
     * @param from message changed from
     * @param to message changed to
     */
    void messageChanged(@NonNull Message from, @NonNull Message to);

    /**
     * Called when removing all the messages.
     */
    void allMessagesRemoved();

}
