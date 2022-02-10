package ru.webim.android.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import ru.webim.android.sdk.impl.MessageReaction;

/**
 * Abstracts a single message in the message history. A message is an immutable object. It means that for changing 
 * some of the message fields there will be created a new object. That is why messages can be compared by using ({@code equals}) for searching messages
 * with the same set of fields or by id ({@code msg1.getId().equals(msg2.getId())}) for searching logically identical messages. Id is formed 
 * on the client side when sending a message ({@link MessageStream#sendMessage} or {@link MessageStream#sendFile}).
 */
public interface Message {
    /**
	 * @return unique client id of the message. Notice that id does not change while changing the content of a message.
     */
    @NonNull Id getClientSideId();

    /**
     * @return session id for current message.
     */
    @Nullable String getSessionId();

    /**
     * @return unique server id of the message.
     */
    @Nullable String getServerSideId();

    /**
     * @return id of a sender, if the sender is an operator
     */
    @Nullable Operator.Id getOperatorId();

    /**
     * @return URL of a sender's avatar
     */
    @Nullable String getSenderAvatarUrl();

    /**
     * @return name of a message sender
     */
    @NonNull String getSenderName();

    /**
     * @return type of a message
     */
    @NonNull Type getType();

    /**
     * @return epoch time (in milliseconds) the message was processed by the server
     */
    long getTime();

    /**
     * @return text of the message
     */
    @NonNull String getText();

    /**
     * @return {@link SendStatus#SENT} if a message had been sent to the server, was received by the server and was 
	 * delivered to all the clients;
     * {@link SendStatus#SENDING} if not
     */
    @NonNull SendStatus getSendStatus();

    /**
     * @return String formatted using JSON if message contains some custom fields
     * of {@link Type#ACTION_REQUEST} Message type.
     * This data is to be unparsed by a client.
     */
    @Nullable String getData();

    /**
     * Messages of the types {@link Type#FILE_FROM_OPERATOR} and {@link Type#FILE_FROM_VISITOR} can contain attachments.
     * Notice that this method may return null even in the case of previously listed types of messages. For instance,
     * if a file is being sent.
     * @return information about the file that is attached to the message
     */
    @Nullable
    Attachment getAttachment();

    /**
     * @return true if this message is history.
     */
    boolean isSavedInHistory();

    /**
     * @return true if this visitor message is read by operator or this message is not by visitor.
     */
    boolean isReadByOperator();

    /**
     * @return true if this message can be edited.
     */
    boolean canBeEdited();

    /**
     * @return true if this message can be replied.
     */
    boolean canBeReplied();

    /**
     * @return true if this message was edited.
     * */
    boolean isEdited();

    /**
     * @return information about quoted message.
     */
    @Nullable Quote getQuote();

    /**
     * @return reaction to a messages.
     */
    @Nullable MessageReaction getReaction();

    /**
     * @return true if messages is possible to react
     */
    boolean canVisitorReact();

    /**
     * @return true if messages is possible to change reaction
     */
    boolean canVisitorChangeReaction();

    /**
     * Abstracts unique id of the message. The class was designed only to be compared by {@code equals}.
     * @see Message#getClientSideId()
     */
    interface Id {
    }

    enum Type {
        /**
         * A message from operator which requests some actions from a visitor.
         * E.g. choose an operator group by clicking on a button in this message.
         * @see Message#getAttachment()
         */
        ACTION_REQUEST,
        /**
         * A message from operator which requests some information about a visitor.
         * @see Message#getAttachment()
         */
        CONTACT_REQUEST,
        /**
         * A message sent by an operator which contains an attachment.
         * Notice that the method {@link Attachment#getAttachment()} may return null even for messages of this type. For instance,
         * if a file is being sent.
         * @see Attachment#getAttachment()
         */
        FILE_FROM_OPERATOR,
        /**
         * A message sent by a visitor which contains an attachment.
         * Notice that the method {@link Attachment#getAttachment()} may return null even for messages of this type. For instance,
         * if a file is being sent.
         * @see Attachment#getAttachment()
         */
        FILE_FROM_VISITOR,
        /**
         * A system information message. Messages of this type are automatically sent at specific events.
         * For example when starting a chat, closing a chat or when an operator joins a chat.
		 */
        INFO,
        /**
         * The system message that the chat bot sends.
         */
        KEYBOARD,
        /**
         * A system message from the chat bot containing information about the button that the user selected.
         */
        KEYBOARD_RESPONSE,
        /**
         * A text message sent by an operator.
         */
        OPERATOR,
        /**
         * A system information message which indicates that an operator is busy and can not reply at the moment.
         */
        OPERATOR_BUSY,
        /**
         * A sticker message sent by a visitor.
         */
        STICKER_VISITOR,
        /**
         * A text message sent by a visitor.
         */
        VISITOR
    }

    /**
     * Until a message will be sent to the server, will have been received by the server and will have been spreaded among clients, we can
	 * see the message as "being sent", at the same time {@link Message#getSendStatus()} will return {@link SendStatus#SENDING}. 
	 * In other cases - {@link SendStatus#SENT}
	 */
    enum SendStatus {
        /**
         * A message is being sent.
         */
        SENDING,
        /**
         * A message had been sent to the server, received by the server and was spreaded among clients.
         */
        SENT
    }

    /**
     * Contains a file attached to the message.
     * @see Message#getAttachment()
     */
    interface Attachment {

        /**
         * @return the fileInfo of the attachment
         */
        @NonNull FileInfo getFileInfo();

        /**
         * @return the filesInfo of the attachment
         */
        @NonNull List<FileInfo> getFilesInfo();

        /**
         * @return type of error in case of problems during attachment upload
         */
        @Nullable String getErrorType();

        /**
         * @return a message with the reason for the error during loading
         */
        @Nullable String getErrorMessage();

        /**
         * @return attachment upload progress as a percentage
         */
        int getDownloadProgress();

        /**
         * @return attachment state
         */
        @NonNull
        AttachmentState getState();

        /**
         * Shows the state of the attachment.
         * @see Attachment#getState()
         */
        enum AttachmentState {
            /**
             * Some error occurred during loading
             */
            ERROR,

            /**
             * Sile is available for download
             */
            READY,

            /**
             * The file is uploaded to the server
             */
            UPLOAD,

            /**
            * The file is verified on server
            */
            EXTERNAL_CHECKS
        }
    }

    /**
     * Contains information about attachment properties.
     * @see Attachment#getFileInfo()
     */
    interface FileInfo {
        /**
         * A URL of a file. 
		 * Notice that this URL is short-lived and is tied to a session.
         * @return url of the file
         */
        @Nullable String getUrl();

        /**
         * @return file size in bytes
         */
        long getSize();

        /**
		 * @return name of a file
         */
        @NonNull String getFileName();

        /**
         * @return MIME-type of a file
         */
        @Nullable String getContentType();

        /**
         * @return if a file is an image, returns information about an image, in other cases returns null
         */
        @Nullable ImageInfo getImageInfo();
    }

    /**
     * Contains information about an image.
     * @see FileInfo#getImageInfo()
     */
    interface ImageInfo {
        /**
         * Returns a URL of a thumbnail. The maximum width and height is usually 300 pixels but it can be adjusted at server settings.
		 * To get an actual preview size before file uploading will be completed, use the following code:
		 * <code><pre>
         *     THUMB_SIZE = 300;
         *     int width = imageInfo.getWidth();
         *     int height = imageInfo.getHeight();
         *     if(height > width) {
         *         width = THUMB_SIZE * width / height;
         *         height = THUMB_SIZE;
         *     } else {
         *         height = THUMB_SIZE * height / width;
         *         width = THUMB_SIZE;
         *     }
         * </pre></code>
		 * Notice that this URL is short-lived and is tied to a session.
         * @return URL уменьшенной копии изображении
         */
        @NonNull String getThumbUrl();

        /**
         * @return width of an image
         */
        int getWidth();

        /**
         * @return height of an image
         */
        int getHeight();
    }

    /**
     * Contains information about quote.
     * @see Message#getQuote()
     */
    interface Quote {
        /**
         * @return the status in which quoted message may be.
         */
        @NonNull State getState();

        /**
         * Messages of the types {@link Type#FILE_FROM_OPERATOR} and {@link Type#FILE_FROM_VISITOR} can contain attachments.
         * Notice that this method may return null even in the case of previously listed types of messages. For instance,
         * if a file is being sent.
         * @return the attachment of the message
         */
        @Nullable FileInfo getMessageAttachment();

        /**
         * @return the id of the message that was quoted.
         */
        @Nullable String getMessageId();

        /**
         * @return type of the message that was quoted.
         */
        @Nullable
        Type getMessageType();

        /**
         * @return name of a message sender of the message that was quoted.
         */
        @Nullable String getSenderName();

        /**
         * @return text of a message sender of the message that was quoted.
         */
        @Nullable String getMessageText();

        /**
         * @return time of the message that was quoted.
         */
        long getMessageTimestamp();

        /**
         * @return author id of the message that was quoted.
         */
        @Nullable String getAuthorId();

        /**
         * Shows the state of the quoted message.
         * @see Quote#getState()
         */
        enum State {

            /**
             * Quoted message send to server.
             */
            PENDING,

            /**
             * Quoted message received by the server and processed.
             */
            FILLED,

            /**
             * Quoted message not found.
             */
            NOT_FOUND
        }
    }

    /**
     * @return keyboard item for chat bot
     */
    @Nullable
    Keyboard getKeyboard();

    /**
     * @return the button that was pressed in keyboard item
     */
    @Nullable
    KeyboardRequest getKeyboardRequest();

    /**
     * Contains information about keyboard item.
     * @see Message#getKeyboard()
     */
    interface Keyboard {
        /**
         * @return List of buttons from keyboard item
         */
        @Nullable List<List<KeyboardButtons>> getButtons();

        /**
         * @return state from keyboard item
         */
        @Nullable Keyboard.State getState();

        /**
         * @return the button that was pressed in keyboard item
         */
        @Nullable
        KeyboardResponse getKeyboardResponse();

        /**
         * Shows the state of the keyboard.
         * @see Keyboard#getState()
         */
        enum State {

            /**
             * Means that all keyboard buttons are active and can be pressed.
             */
            PENDING,

            /**
             * Means that all keyboard buttons are inactive and can't be pressed.
             */
            CANCELLED,

            /**
             * Means that selected button is displayed pressed others are inactive.
             */
            COMPLETED,
        }
    }

    /**
     * Contains information about buttons in keyboard item.
     * @see Keyboard#getButtons()
     */
    interface KeyboardButtons {
        /**
         * @return the id of the button in the keyboard item that will be shown in the chat
         */
        @NonNull String getId();

        /**
         * @return the text of the button in the keyboard item that will be shown in the chat
         */
        @NonNull String getText();

        /**
         * @return the configuration of the button
         */
        @Nullable Configuration getConfiguration();

        /**
         * @see KeyboardButtons#getConfiguration()
         */
        interface Configuration {

            /**
             * @return button type
             */
            @NonNull
            Type getButtonType();

            /**
             * @return url if type URL_BUTTON, text if INSERT_BUTTON
             */
            @NonNull String getData();

            /**
             * @return button state
             */
            @NonNull State getState();

            /**
             * @see Configuration#getType()
             */
            enum Type {
                URL_BUTTON,
                INSERT_BUTTON
            }


            /**
             * @see Configuration#getState() ()
             */
            enum State {
                SHOWING,
                SHOWING_SELECTED,
                HIDDEN
            }
        }
    }

    /**
     * Contains information about the pressed button in keyboard item.
     * @see Keyboard#getKeyboardResponse()
     */
    interface KeyboardResponse {
        /**
         * @return the button id that was pressed in keyboard item
         */
        @NonNull String getButtonId();

        /**
         * @return the button text that was pressed in keyboard item
         */
        @NonNull String getMessageId();
    }

    /**
     * Contains information about the pressed button in keyboard item.
     * @see Message#getKeyboardRequest()
     */
    interface KeyboardRequest {
        /**
         * @return the button that was pressed in keyboard item
         */
        @Nullable
        KeyboardButtons getButtons();

        /**
         * @return the message id that was pressed in keyboard item
         */
        @NonNull String getMessageId();
    }

    /**
     * @return the sticker item that was sent to the server.
     */
    @Nullable
    Sticker getSticker();

    /**
     * Contains information about sticker.
     * @see Message#getSticker()
     */
    interface Sticker {

        /**
         * @return sticker id
         */
        int getStickerId();
    }
}
