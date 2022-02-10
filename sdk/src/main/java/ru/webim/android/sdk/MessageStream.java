package ru.webim.android.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.Date;
import java.util.List;

import ru.webim.android.sdk.impl.MessageReaction;

/**
 * @see WebimSession#getStream()
 */
public interface MessageStream {
    /**
     * @see VisitSessionState
     * @return current session state
     */
    @NonNull
    VisitSessionState getVisitSessionState();

    /**
     * @return current chat state
     * @see ChatState
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was created in
     */
    @NonNull
    ChatState getChatState();

    /**
     * Operator of the current chat
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was created in
     */
    @Nullable
    Operator getCurrentOperator();

    /**
     * @return current LocationSettings of the MessageStream.
     */
    @NonNull
    LocationSettings getLocationSettings();

    /**
     * @return previous rating of the operator or 0 if the operator was not rated before
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was created in
     */
    int getLastOperatorRating(Operator.Id operatorId);


    /**
     * @return timestamp of first unread message by operator
     */
    @Nullable
    Date getUnreadByOperatorTimestamp();

    /**
     * @return timestamp of first unread message by visitor
     */
    @Nullable
    Date getUnreadByVisitorTimestamp();

    /**
     * @return count of unread messages by visitor
     */
    int getUnreadByVisitorMessageCount();

    /**
     * @see Department
     * @see DepartmentListChangeListener
     * @return List of departments or null if there're any or department list is not received yet
     */
    @Nullable
    List<Department> getDepartmentList();

    /**
     * Rates an operator. To get an id of the current operator use
     * {@link MessageStream#getCurrentOperator()}
     * @see RateOperatorCallback
     * @param operatorId id of the operator to be rated
     * @param rate a number in range [1, 5]
     * @param rateOperatorCallback callback handler object
     * @throws IllegalArgumentException if 'rate' is not in range [1, 5]
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    @CustomMethod
    void rateOperator(@NonNull Operator.Id operatorId,
                      int rate,
                      @Nullable RateOperatorCallback rateOperatorCallback);

    /**
     * Rates an operator. To get an id of the current operator use
     * {@link MessageStream#getCurrentOperator()}
     * @see RateOperatorCallback
     * @param operatorId id of the operator to be rated
     * @param rate a number in range [1, 5]
     * @param rateOperatorCallback callback handler object
     * @throws IllegalArgumentException if 'rate' is not in range [1, 5]
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    void rateOperator(@NonNull String operatorId,
                      int rate,
                      @Nullable RateOperatorCallback rateOperatorCallback);

    /**
     * Rates an operator. To get an id of the current operator use
     * {@link MessageStream#getCurrentOperator()}
     * @see RateOperatorCallback
     * @param operatorId id of the operator to be rated
     * @param note a comment for rating
     * @param rate a number in range [1, 5]
     * @param rateOperatorCallback callback handler object
     * @throws IllegalArgumentException if 'rate' is not in range [1, 5]
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    void rateOperator(@NonNull Operator.Id operatorId,
                      @Nullable String note,
                      int rate,
                      @Nullable RateOperatorCallback rateOperatorCallback);

    /**
     * @param id id of redirect to sentry message
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    @CustomMethod
    void respondSentryCall(String id);

    /**
     * @param query query to be searched
     * @param callback callback for handling result
     * */
    @CustomMethod
    void searchMessages(@NonNull String query,
                        @Nullable SearchMessagesCallback callback);

    /**
     * Changes {@link ChatState} to {@link ChatState#QUEUE}
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread {@link WebimSession}
     * was created in
     */
    void startChat();

    /**
     * @param message message that is reacted
     * @param reaction reaction for message
     * @param callback callback for handling result
     */
    @CustomMethod
    void reactMessage(@NonNull Message message,
                      @NonNull MessageReaction reaction,
                      @Nullable MessageReactionCallback callback);

    /**
     * Starts chat with particular department.
     * Changes {@link ChatState} to {@link ChatState#QUEUE}.
     * @see Department
     * @param departmentKey department key
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread {@link WebimSession}
     * was created in
     */
    void startChatWithDepartmentKey(@Nullable String departmentKey);

    /**
     * Starts chat and sends first message simultaneously.
     * Changes {@link ChatState} to {@link ChatState#QUEUE}.
     * @param firstQuestion first visitor message to send
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread {@link WebimSession}
     * was created in
     * */
    void startChatWithFirstQuestion(@Nullable String firstQuestion);

    /**
     * Starts chat with custom fields.
     * Changes {@link ChatState} to {@link ChatState#QUEUE}.
     * @param customFields custom fields
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread {@link WebimSession}
     * was created in
     */
    @CustomMethod
    void startChatWithCustomFields(@Nullable String customFields);

    /**
     * Starts chat with particular department and sends first message simultaneously.
     * Changes {@link ChatState} to {@link ChatState#QUEUE}.
     * @see Department
     * @param departmentKey department key
     * @param firstQuestion first visitor message to send
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread {@link WebimSession}
     * was created in
     */
    void startChatWithDepartmentKeyFirstQuestion(@Nullable String departmentKey,
                                                 @Nullable String firstQuestion);

    /**
     * Starts chat with custom fields and sends first message simultaneously.
     * Changes {@link ChatState} to {@link ChatState#QUEUE}.
     * @param customFields custom fields
     * @param firstQuestion first visitor message to send
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread {@link WebimSession}
     * was created in
     */
    void startChatWithCustomFieldsFirstQuestion(@Nullable String customFields,
                                                @Nullable String firstQuestion);

    /**
     * Starts chat with custom fields and sends first message simultaneously.
     * Changes {@link ChatState} to {@link ChatState#QUEUE}.
     * @see Department
     * @param customFields custom fields
     * @param departmentKey department key
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread {@link WebimSession}
     * was created in
     */
    void startChatWithCustomFieldsDepartmentKey(@Nullable String customFields,
                                                @Nullable String departmentKey);

    /**
     * Starts chat with custom fields and particular department and sends first message simultaneously.
     * Changes {@link ChatState} to {@link ChatState#QUEUE}.
     * @see Department
     * @param firstQuestion first visitor message to send
     * @param customFields custom fields
     * @param departmentKey department key
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread {@link WebimSession}
     * was created in
     */
    void startChatWithFirstQuestionCustomFieldsDepartmentKey(@Nullable String firstQuestion,
                                                             @Nullable String customFields,
                                                             @Nullable String departmentKey);

    /**
     * Changes {@link ChatState} to {@link ChatState#CLOSED_BY_VISITOR}
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was created in
     */
    void closeChat();

    /**
     * Set chat has been read by visitor.
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was created in
     */
    void setChatRead();

    /**
     * Sends prechat fields to server.
     * @param prechatFields custom fields
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was created in
     */
    @CustomMethod
    void setPrechatFields(String prechatFields);

    /**
     * This method must be called whenever there is a change of the input field of a message transferring current content 
	 * of a message as a parameter.
     * @param draftMessage current message content
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was created in
     */
    void setVisitorTyping(@Nullable String draftMessage);

    /**
     * Sends a text message.
     * When calling this method, if there is an active {@link MessageTracker} (see
     * {@link MessageStream#newMessageTracker}),
     * {@link MessageListener#messageAdded(Message, Message)} with a message
     * {@link Message.SendStatus#SENDING} in the status is also called.
     * @param message text of the message
     * @return id of the message
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    @NonNull
    Message.Id sendMessage(@NonNull String message);

    /**
     * Sends a text message.
     * When calling this method, if there is an active {@link MessageTracker} (see
     * {@link MessageStream#newMessageTracker}),
     * {@link MessageListener#messageAdded(Message, Message)} with a message
     * {@link Message.SendStatus#SENDING} in the status is also called.
     * @see Message#getAttachment()
     * @param message text of the message
     * @param data custom message parameters fields formatted to JSON string
     * @param dataMessageCallback shows if a call is completed or failed
     * @return id of the message
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    @CustomMethod
    @NonNull
    Message.Id sendMessage(@NonNull String message,
                           @Nullable String data,
                           @Nullable DataMessageCallback dataMessageCallback);

    /**
     * Sends a text message.
     * When calling this method, if there is an active {@link MessageTracker} (see
     * {@link MessageStream#newMessageTracker}),
	 * {@link MessageListener#messageAdded(Message, Message)} with a message
     * {@link Message.SendStatus#SENDING} in the status is also called.
     * @param message text of the message
     * @param isHintQuestion is optional. Shows to server if a visitor chose a hint (true) or wrote
     *                       his own text (false)
     * @return id of the message
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    @NonNull
    Message.Id sendMessage(@NonNull String message, boolean isHintQuestion);

    /**
     * Sends a message with file description.
     * When calling this method, if there is an active {@link MessageTracker} (see
     * {@link MessageStream#newMessageTracker}),
     * {@link MessageListener#messageAdded(Message, Message)} with a message
     * {@link Message.SendStatus#SENDING} in the status is also called.
     * @param uploadedFiles files description
     * @param sendFilesCallback shows if a call is completed or failed
     * @return id of the message
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    @CustomMethod
    @NonNull
    Message.Id sendFiles(@NonNull List<UploadedFile> uploadedFiles,
                         @Nullable SendFilesCallback sendFilesCallback);

    /**
     * Sends a reply on message.
     * When calling this method, if there is an active {@link MessageTracker} (see
     * {@link MessageStream#newMessageTracker}),
     * {@link MessageListener#messageAdded(Message, Message)} with a message
     * {@link Message.SendStatus#SENDING} in the status is also called.
     * @param message text of the message
     * @param quotedMessage the quoted message
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    boolean replyMessage(@NonNull String message, @NonNull Message quotedMessage);

    /**
     * Update widget status. The change is displayed by the operator.
     * @param data JSON string with new widget status
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    void updateWidgetStatus(@NonNull String data);

    /**
     * Sends a button from keyboard.
     * When calling this method, if there is an active {@link MessageTracker} (see
     * {@link MessageStream#newMessageTracker}),
     * {@link MessageListener#messageAdded(Message, Message)} with a message
     * @see Message#getKeyboard()
     * @param requestMessageCurrentChatId currentChatId of the message
     * @param buttonId the id of the button that the user selected
     * @param sendKeyboardCallback shows if a call is completed or failed
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    void sendKeyboardRequest(@NonNull String requestMessageCurrentChatId,
                             @NonNull String buttonId,
                             @Nullable SendKeyboardCallback sendKeyboardCallback);

    /**
     * Edits a previously sent text message.
     * When calling this method, if there is an active {@link MessageTracker} (see
     * {@link MessageStream#newMessageTracker}),
     * {@link MessageListener#messageChanged(Message, Message)} with a message
     * {@link Message.SendStatus#SENDING} in the status is also called.
     * @param message the message to edit
     * @param text new text of the message
     * @param editMessageCallback shows if a call is completed or failed
     * @return true if the message can be edited
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    boolean editMessage(@NonNull Message message,
                        @NonNull String text,
                        @Nullable EditMessageCallback editMessageCallback);

    /**
     * Deletes a message.
     * When calling this method, if there is an active {@link MessageTracker} (see
     * {@link MessageStream#newMessageTracker}),
     * {@link MessageListener#messageRemoved(Message)} with a message
     * {@link Message.SendStatus#SENT} in the status is also called.
     * @param message the message to delete
     * @param deleteMessageCallback shows if a call is completed or failed
     * @return true if the message can be deleted
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    boolean deleteMessage(@NonNull Message message,
                          @Nullable DeleteMessageCallback deleteMessageCallback);

    /**
     * Sends a file message. A name of a file must contain file extension which is appropriate to
     * the  specified MIME type. (use {@link android.webkit.MimeTypeMap}).
     * When calling this method, if there is an active {@link MessageTracker} (see
     * {@link MessageStream#newMessageTracker}),
     * {@link MessageListener#messageAdded(Message, Message)} with a message
     * {@link Message.SendStatus#SENDING} in the status is also called.
     * @param file the file to send
     * @param name the name of the file (must end with a valid extension)
     * @param mimeType MIME type of the file
     * @param callback shows if a call on file upload is completed or failed
     * @return id of the message
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    @NonNull
    Message.Id sendFile(@NonNull File file,
                        @NonNull String name,
                        @NonNull String mimeType,
                        @Nullable SendFileCallback callback);

    /**
     * Uploads the file to the server. A name of a file must contain file extension which is appropriate to
     * the  specified MIME type. (use {@link android.webkit.MimeTypeMap}).
     * @param file the file to send
     * @param name the name of the file (must end with a valid extension)
     * @param mimeType MIME type of the file
     * @param uploadFileToServerCallback shows if a call on file upload is completed or failed
     * @return id of the message
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    @CustomMethod
    @NonNull
    Message.Id uploadFilesToServer(@NonNull File file,
                                   @NonNull String name,
                                   @NonNull String mimeType,
                                   @Nullable UploadFileToServerCallback uploadFileToServerCallback);

    /**
     * Deletes files uploaded to the server ({@link MessageStream#uploadFilesToServer}).
     * @param fileGuid guid file to delete
     * @param deleteUploadedFileCallback shows if a call on file delete is completed or failed
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    @CustomMethod
    void deleteUploadedFiles(@NonNull String fileGuid,
                             @Nullable DeleteUploadedFileCallback deleteUploadedFileCallback);

    /**
     * Send sticker to chat.
     * When calling this method, if there is an active {@link MessageTracker} (see
     * {@link MessageStream#newMessageTracker}),
     * {@link MessageListener#messageAdded(Message, Message)} with a message
     * @param stickerId contains the id of the sticker to send
     * @param sendStickerCallback shows if the call to send the sticker is completed or not
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    @CustomMethod
    void sendSticker(int stickerId, @Nullable SendStickerCallback sendStickerCallback);

    /**
     * Receive raw location config from server.
     * @param location location name
     * @param callback shows if the call to receive location config is completed or not
     * */
    void getRawLocationConfig(@NonNull String location, @NonNull RawLocationConfigCallback callback);

    /**
     * MessageTracker (via {@link MessageTracker#getNextMessages}) allows to request the messages which are above in the history.
     * Each next call {@link MessageTracker#getNextMessages} returns earlier messages in relation to the already requested ones.
     * Changes of user-visible messages (i.e. ever requested from MessageTracker) are transmitted to
     * {@link MessageListener}. That is why {@link MessageListener} is needed when creating {@link MessageTracker}.
     * For each {@link MessageStream} at every single moment can exist the only active {@link MessageTracker}.
     * When creating a new one at the previous there will be automatically called {@link MessageTracker#destroy()}.
     * @param listener a listener of message changes in the tracking range
     * @return a new {@link MessageTracker} for this stream
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was created in
     */
    @NonNull
    MessageTracker newMessageTracker(@NonNull MessageListener listener);

    /**
     * Sets {@link VisitSessionStateListener} object.
     * @see VisitSessionStateListener
     * @see VisitSessionState
     * @param visitSessionStateListener {@link VisitSessionStateListener} object
     */
    void setVisitSessionStateListener(@NonNull VisitSessionStateListener visitSessionStateListener);

    /**
     * Sets the {@link ChatState} change listener
     * @param listener the {@link ChatState} change listener
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was created in
     */
    void setChatStateListener(@NonNull ChatStateListener listener);

    /**
     * Sets the current {@link Operator} change listener
     * @param listener the current {@link Operator} change listener
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was created in
     */
    void setCurrentOperatorChangeListener(@NonNull CurrentOperatorChangeListener listener);

    /**
     * Sets the listener of the 'operator typing' status changes
     * @param listener the listener of the 'operator typing' status changes
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was created in
     */
    void setOperatorTypingListener(@NonNull OperatorTypingListener listener);

    /**
     * Sets {@link DepartmentListChangeListener} object.
     * @see DepartmentListChangeListener
     * @see Department
     * @param departmentListChangeListener {@link DepartmentListChangeListener} object
     */
    void setDepartmentListChangeListener
    (@NonNull DepartmentListChangeListener departmentListChangeListener);

    /**
     * Sets the listener of the MessageStream LocationSettings changes.
     * @param locationSettingsChangeListener
     */
    void setLocationSettingsChangeListener(LocationSettingsChangeListener locationSettingsChangeListener);


    /**
     * Sets the listener of {@link WebimSession} online status changes.
     * @param onlineStatusChangeListener OnlineStatusChange listener object
     * @see OnlineStatusChangeListener
     */
    void setOnlineStatusChangeListener(OnlineStatusChangeListener onlineStatusChangeListener);

    /**
     * Sets listener for parameter that is to be returned by
     * {@link MessageStream#getUnreadByOperatorTimestamp()} method.
     * @param listener {@link UnreadByOperatorTimestampChangeListener} object
     */
    void setUnreadByOperatorTimestampChangeListener(
            @Nullable UnreadByOperatorTimestampChangeListener listener
    );

    /**
     * Sets listener for parameter that is to be returned by
     * {@link MessageStream#getUnreadByVisitorTimestamp()} method.
     * @param listener {@link UnreadByVisitorTimestampChangeListener} object
     */
    void setUnreadByVisitorTimestampChangeListener(
            @Nullable UnreadByVisitorTimestampChangeListener listener
    );

    /**
     * Sets listener for parameter that is to be returned by
     * {@link MessageStream#getUnreadByVisitorMessageCount()} method.
     * @param listener {@link UnreadByVisitorMessageCountChangeListener} object
     */
    void setUnreadByVisitorMessageCountChangeListener(
            @Nullable UnreadByVisitorMessageCountChangeListener listener
    );

    /**
     * This method clears the all history on the server
     */
    @CustomMethod
    void clearChatHistory();

    /**
     * Sending chat history on email
     * @see SendDialogToEmailAddressCallback
     * @param email this is the mail to which chat history will be sent
     * @param sendDialogToEmailAddressCallback shows if a call is completed or failed
     * @throws IllegalStateException if the WebimSession was destroyed
     * @throws RuntimeException if the method was called not from the thread the WebimSession was
     * created in
     */
    void sendDialogToEmailAddress(@NonNull String email,
                                  @NonNull SendDialogToEmailAddressCallback sendDialogToEmailAddressCallback);

    /**
     * Sets listener for greeting message
     * @param listener {@link GreetingMessageListener} object
     */
    @CustomMethod
    void setGreetingMessageListener(@NonNull GreetingMessageListener listener);

    /**
     * @see MessageStream#setGreetingMessageListener(GreetingMessageListener)
     */
    interface GreetingMessageListener {
        /**
         * Invoked when greeting message is available. Method is called at the beginning of chat
         * when no messages have been sent yet.
         * @param message greeting message
         */
        void greetingMessage(@NonNull String message);
    }

    /**
     * Sending survey answer.
     * @param surveyAnswer answer to survey. If question type is 'stars', answer is var 1-5 that
     * corresponds the rating. If question type is 'radio', answer is index of element in options
     * array beginning with 1. If question type is 'comment', answer is a string.
     * @param callback shows if the call to send the survey answer is completed or not
     * @see SurveyAnswerCallback
     * @see Survey.Question.Type
     */
    @CustomMethod
    void sendSurveyAnswer(@NonNull String surveyAnswer,
                          @Nullable SurveyAnswerCallback callback);

    /**
     * Method closes current survey.
     * @param callback shows if the call to close the survey is completed or not.
     * @see SurveyCloseCallback
     */
    @CustomMethod
    void closeSurvey(@Nullable SurveyCloseCallback callback);

    /**
     * Set listener, that will be called when new survey will come.
     * @param surveyListener {@link SurveyListener} object
     */
    @CustomMethod
    void setSurveyListener(@NonNull SurveyListener surveyListener);

    /**
     * @see MessageStream#sendSurveyAnswer(String, SurveyAnswerCallback)
     */
    interface SurveyAnswerCallback {
        /**
         * Invoked when survey answer is sent successfully.
         */
        void onSuccess();

        /**
         * Invoked when an error occurred while sending survey answer.
         */
        void onFailure(WebimError<SurveyAnswerError> webimError);

        /**
         * @see SurveyAnswerCallback#onFailure(WebimError)
         */
        enum SurveyAnswerError {

            /**
             * Incorrect value for question type 'radio'.
             */
            INCORRECT_RADIO_VALUE,

            /**
             * Incorrect value for question type 'stars'.
             */
            INCORRECT_STARS_VALUE,

            /**
             * Incorrect survey id.
             */
            INCORRECT_SURVEY_ID,

            /**
             * Max comment length was exceeded for question type 'comment'.
             */
            MAX_COMMENT_LENGTH_EXCEEDED,

            /**
             * No current survey on server.
             */
            NO_CURRENT_SURVEY,

            /**
             * Question was not found.
             */
            QUESTION_NOT_FOUND,

            /**
             * Survey is disabled on server.
             */
            SURVEY_DISABLED,

            /**
             * Received error is not supported by current WebimClientLibrary version.
             */
            UNKNOWN
        }
    }

    /**
     * @see MessageStream#closeSurvey(SurveyCloseCallback)
     */
    interface SurveyCloseCallback {

        /**
         * Invoked when survey was successfully closed on server.
         */
        void onSuccess();

        /**
         * Invoked when an error occurred while closing a survey on server.
         */
        void onFailure(WebimError<SurveyCloseCallback.SurveyCloseError> webimError);

        /**
         * @see SurveyCloseCallback#onFailure(WebimError)
         */
        enum SurveyCloseError {

            /**
             * Incorrect survey id.
             */
            INCORRECT_SURVEY_ID,

            /**
             * No current survey on server.
             */
            NO_CURRENT_SURVEY,

            /**
             * Survey is disabled on server.
             */
            SURVEY_DISABLED,

            /**
             * Received error is not supported by current WebimClientLibrary version.
             */
            UNKNOWN
        }
    }

    /**
     * @see MessageStream#searchMessages(String, SearchMessagesCallback)
     * */
    interface SearchMessagesCallback {
        /**
         * @param messages list of messages that corresponds to search query.<br/>
         * Empty list if corresponding messages were not found.
         * */
        void onResult(@NonNull List<? extends Message> messages);
    }

    /**
     * @see MessageStream#getRawLocationConfig(String, RawLocationConfigCallback)
     */
    interface RawLocationConfigCallback {
        /**
         * Invoked when location config received successfully.
         */
        void onSuccess(String jsonLocationConfig);

        /**
         * Invoked when an error occurred while receiving location config.
         */
        void onFailure();
    }

    /**
     * @see MessageStream#reactMessage(Message, MessageReaction, MessageReactionCallback)
     * */
    interface MessageReactionCallback {

        /**
         * Invoked when message was successfully reacted
         */
        void onSuccess(@NonNull Message.Id messageId);

        /**
         * Invoked when an error occurred while reacting message
         */
        void onFailure(@NonNull Message.Id messageId,
                       @NonNull WebimError<MessageReactionError> error);

        /**
         * @see MessageReactionCallback#onFailure(Message.Id, WebimError)
         */
        enum MessageReactionError {

            /**
             * When reaction of this message can't change
             */
            UNABLE_CHANGE_REACTION,

            /**
             * When message reaction is forbidden, because of server settings or visitor message
             */
            REACTION_FORBIDDEN,

            /**
             * Message with the specified id is not found in history.
             */
            MESSAGE_NOT_FOUND,

            /**
             * Reacting message is turned off on the server.
             */
            NOT_ALLOWED,

            /**
             * Reacting message doesn't belong visitor.
             */
            MESSAGE_NOT_OWNED,

            /**
             * Unknown error.
             */
            UNKNOWN,
        }
    }

    /**
     * @see MessageStream#sendMessage(String, String, DataMessageCallback)
     */
    interface DataMessageCallback {
        /**
         * Invoked when message with data field is sent successfully.
         * @param messageId ID of the message
         */
        void onSuccess(@NonNull Message.Id messageId);

        /**
         * Invoked when an error occurred while sending message with data.
         * @param messageId ID of the message
         * @param dataMessageError Error
         * @see DataMessageError
         */
        void onFailure(@NonNull Message.Id messageId,
                       @NonNull WebimError<DataMessageError> dataMessageError);

        /**
         * @see DataMessageCallback#onFailure(Message.Id, WebimError)
         */
        enum DataMessageError {
            /**
             * Received error is not supported by current WebimClientLibrary version.
             */
            UNKNOWN,

            // Quoted message errors.

            /**
             * To be raised when quoted message ID belongs to a message without `canBeReplied` flag
             * set to `true` (this flag is to be set on the server-side).
             */
            QUOTED_MESSAGE_CANNOT_BE_REPLIED,

            /**
             * To be raised when quoted message ID belongs to another visitor's chat.
             */
            QUOTED_MESSAGE_FROM_ANOTHER_VISITOR,

            /**
             * To be raised when quoted message ID belongs to multiple messages (server DB error).
             */
            QUOTED_MESSAGE_MULTIPLE_IDS,

            /**
             * To be raised when one or more required arguments of quoting mechanism are missing.
             */
            QUOTED_MESSAGE_REQUIRED_ARGUMENTS_MISSING,

            /**
             * To be raised when wrong quoted message ID is sent.
             */
            QUOTED_MESSAGE_WRONG_ID
        }
    }

    /**
     * @see MessageStream#sendFiles(List, SendFilesCallback)
     */
    interface SendFilesCallback {

        /**
         * Invoked when message with files is sent successfully.
         * @param messageId ID of the message
         */
        void onSuccess(@NonNull Message.Id messageId);

        /**
         * Invoked when an error occurred while sending message with files.
         * @param messageId ID of the message
         * @param sendFilesError Error
         * @see DataMessageCallback.DataMessageError
         */
        void onFailure(@NonNull Message.Id messageId,
                       @NonNull WebimError<SendFileCallback.SendFileError> sendFilesError);
    }

    /**
     * @see MessageStream#sendKeyboardRequest(String, String, SendKeyboardCallback)
     */
    interface SendKeyboardCallback {

        /**
         * Invoked when message with data field is sent successfully.
         * @param messageId ID of the message
         */
        void onSuccess(@NonNull Message.Id messageId);

        /**
         * Invoked when file deleting failed.
         * @param messageId ID of the message
         * @param error Error
         * @see SendKeyboardError
         */
        void onFailure(@NonNull Message.Id messageId,
                       @NonNull WebimError<SendKeyboardError> error);

        /**
         * @see SendKeyboardCallback#onFailure(Message.Id, WebimError)
         */
        enum SendKeyboardError {
            /**
             * Button from keyboard sent to the wrong chat
             */
            NO_CHAT,
            /**
             * Not set button id
             */
            BUTTON_ID_NO_SET,
            /**
             * Not set request message id
             */
            REQUEST_MESSAGE_ID_NOT_SET,
            /**
             * Cannot create response
             */
            CAN_NOT_CREATE_RESPONSE,
            /**
             * Received error is not supported by current WebimClientLibrary version.
             */
            UNKNOWN
        }
    }

    /**
     * @see MessageStream#editMessage(Message, String, EditMessageCallback)
     */
    interface EditMessageCallback {
        /**
         * Invoked when file editing succeed.
         * @param id ID of the message
         * @param text new text of a message
         */
        void onSuccess(@NonNull Message.Id id, @NonNull String text);

        /**
         * Invoked when file deleting failed.
         * @param id ID of the message
         * @param error Error
         * @see EditMessageError
         */
        void onFailure(@NonNull Message.Id id, @NonNull WebimError<EditMessageError> error);

        /**
         * @see EditMessageCallback#onFailure
         */
        enum EditMessageError {
            /**
             * Editing messages by visitor is turned off on the server.
             */
            NOT_ALLOWED,
            /**
             * Editing message is empty.
             */
            MESSAGE_EMPTY,
            /**
             * Visitor can edit only his messages.
             * The specified id belongs to someone else's message.
             */
            MESSAGE_NOT_OWNED,
            /**
             * The server may deny a request if the message size exceeds a limit.
             * The maximum size of a message is configured on the server.
             */
            MAX_LENGTH_EXCEEDED,
            /**
             * Visitor can edit only text messages.
             */
            WRONG_MESSAGE_KIND,
            /**
             * Received error is not supported by current WebimClientLibrary version.
             */
            UNKNOWN
        }
    }

    /**
     * @see MessageStream#deleteMessage(Message, DeleteMessageCallback)
     */
    interface DeleteMessageCallback {
        /**
         * Invoked when file deleting succeed.
         * @param id ID of the message
         */
        void onSuccess(@NonNull Message.Id id);

        /**
         * Invoked when file deleting failed.
         * @param id ID of the message
         * @param error Error
         * @see DeleteMessageError
         */
        void onFailure(@NonNull Message.Id id, @NonNull WebimError<DeleteMessageError> error);

        /**
         * @see DeleteMessageCallback#onFailure
         */
        enum DeleteMessageError {
            /**
             * Deleting messages by visitor is turned off on the server.
             */
            NOT_ALLOWED,
            /**
             * Visitor can delete only his messages.
             * The specified id belongs to someone else's message.
             */
            MESSAGE_NOT_OWNED,
            /**
             * Message with the specified id is not found in history.
             */
            MESSAGE_NOT_FOUND,
            /**
             * Received error is not supported by current WebimClientLibrary version.
             */
            UNKNOWN
        }
    }

    /**
     * @see MessageStream#sendFile
     */
    interface SendFileCallback {
        /**
         * Not yet implemented
         */
        @Deprecated
        void onProgress(@NonNull Message.Id id, long sentBytes);

        /**
         * Invoked when file sending succeed.
         * @param id ID of the message
         */
        void onSuccess(@NonNull Message.Id id);

        /**
         * Invoked when file sending failed.
         * @param id ID of the message
         * @param error Error
         * @see SendFileError
         */
        void onFailure(@NonNull Message.Id id, @NonNull WebimError<SendFileError> error);

        /**
         * @see SendFileCallback#onFailure
         */
        enum SendFileError {
            /**
             * File upload timeout exceeded
             */
            CONNECTION_TIMEOUT,
            /**
             * Upload file not found on device.
             */
            FILE_NOT_FOUND,
            /**
             * The server may deny a request if the file type is not allowed.
             * The list of allowed file types is configured on the server.
             */
            FILE_TYPE_NOT_ALLOWED,
            /**
             * The server may deny a request if the file size exceeds a limit.
             * The maximum size of a file is configured on the server.
             */
            FILE_SIZE_EXCEEDED,
            /**
             * The file uploaded to the server is too small
             */
            FILE_SIZE_TOO_SMALL,
            /**
             * The maximum number of files uploaded per chat
             */
            MAX_FILES_COUNT_PER_CHAT_EXCEEDED,
            /**
             * The message has exceeded the maximum number of files.
             */
            MAX_FILES_COUNT_PER_MESSAGE,
            /**
             * Sending files in body is not supported. Use multipart form only.
             * */
            UPLOADED_FILE_NOT_FOUND,
            /**
             * Received error is not supported by current WebimClientLibrary version.
             */
            UNKNOWN,
            /**
             * The upload file has an incorrect or erroneous name
             */
            FILE_NAME_INCORRECT,

            /**
             * Sending file is empty
             */
            FILE_IS_EMPTY,
            /**
             * Visitor authorization error on the server
             */
            UNAUTHORIZED
        }
    }

    /**
     * @see MessageStream#uploadFilesToServer
     */
    interface UploadFileToServerCallback {
        /**
         * Invoked when file sending succeed.
         * @param id ID of the message
         * @param uploadedFile description of the uploaded file
         */
        void onSuccess(@NonNull Message.Id id, @NonNull UploadedFile uploadedFile);

        /**
         * Invoked when file sending failed.
         * @param id ID of the message
         * @param error Error
         * @see SendFileCallback.SendFileError
         */
        void onFailure(@NonNull Message.Id id, @NonNull WebimError<SendFileCallback.SendFileError> error);
    }

    /**
     * @see MessageStream#deleteUploadedFiles
     */
    interface DeleteUploadedFileCallback {
        /**
         * Invoked when file deleting succeed.
         */
        void onSuccess();

        /**
         * Invoked when file deleting failed.
         * @param error Error
         * @see DeleteUploadedFileCallback.DeleteUploadedFileError
         */
        void onFailure(@NonNull WebimError<DeleteUploadedFileError> error);

        /**
         * @see DeleteUploadedFileCallback#onFailure
         */
        enum DeleteUploadedFileError {
            /**
             * File not found on server
             */
            FILE_NOT_FOUND,
            /**
             * The file is attached to the message and cannot be deleted
             */
            FILE_HAS_BEEN_SENT,
            /**
             * Received error is not supported by current WebimClientLibrary version.
             */
            UNKNOWN
        }
    }

    /**
     * @see MessageStream#rateOperator(Operator.Id, int, RateOperatorCallback)
     */
    interface RateOperatorCallback {
        /**
         * Called if rate operator request succeed.
         */
        void onSuccess();

        /**
         * Called if rate operator request failed.
         * @param rateOperatorError error
         */
        void onFailure(@NonNull WebimError<RateOperatorError> rateOperatorError);

        enum RateOperatorError {
            /**
             * Arises when there was a try to rate an operator if there's no chat exists.
             */
            NO_CHAT,
            /**
             * Arises when there was a try to rate an operator if this operator does not belong to
             * existing chat.
             */
            OPERATOR_NOT_IN_CHAT,

            /**
             * Note length is more than 2000 characters.
             */
            NOTE_IS_TOO_LONG
        }
    }

    /**
     * A chat is seen in different ways by an operator depending on ChatState. The initial state is {@link ChatState#NONE}.
     * Then if a visitor sends a message ({@link MessageStream#sendMessage(String)}), the chat changes it's state to {@link ChatState#QUEUE}.
     * The chat can be turned into this state by calling {@link MessageStream#startChat()}. After that, if an operator takes the chat to process,
     * the state changes to {@link ChatState#CHATTING}. The chat is being in this state until the visitor or the operator closes it.
     * When closing a chat by the visitor {@link MessageStream#closeChat()}, it turns into the state {@link ChatState#CLOSED_BY_VISITOR},
     * by the operator - {@link ChatState#CLOSED_BY_OPERATOR}. When both the visitor and the operator close the chat, it's state changes to the initial
     * {@link ChatState#NONE}. A chat can also automatically turn into the initial state during long-term absence of activity in it.
     * Furthermore, the first message can be sent not only by a visitor but also by an operator. In this case the state will change from the initial to
     * {@link ChatState#INVITATION}, and then, after the first message of the visitor, it changes to {@link ChatState#CHATTING}.
     */
    enum ChatState {
        /**
         * Means that an operator has taken a chat for processing
         * From this state a chat can be turned into:
         * <ul>
         * <li>{@link ChatState#CLOSED_BY_OPERATOR}, if an operator closes the chat</li>
         * <li>{@link ChatState#CLOSED_BY_VISITOR}, if a visitor closes the chat ({@link MessageStream#closeChat()})</li>
         * <li>{@link ChatState#NONE}, automatically during long-term absence of activity</li>
         * </ul>
         */
        CHATTING,

        /**
         * Means that chat is picked up by a bot.
         * From this state a chat can be turned into:
         * <ul>
         * <li>{@link ChatState#CHATTING}, if an operator intercepted the chat</li>
         * <li>{@link ChatState#CLOSED_BY_VISITOR}, if a visitor closes the chat ({@link MessageStream#closeChat()})</li>
         * <li>{@link ChatState#ROUTING}, if canceled by the robot</li>
         * <li>{@link ChatState#NONE}, automatically during long-term absence of activity.</li>
         * </ul>
         */
        CHATTING_WITH_ROBOT,

        /**
         * Means that an operator has closed the chat.
         * From this state a chat can be turned into:
         * <ul>
         * <li>{@link ChatState#CHATTING}, if an operator intercepted the chat</li>
         * <li>{@link ChatState#NONE}, if the chat is also closed by a visitor ({@link MessageStream#closeChat()}), or automatically during long-term absence of activity</li>
         * <li>{@link ChatState#QUEUE}, if a visitor sends a new message({@link MessageStream#sendMessage(String)})</li>
         * </ul>
         */
        CLOSED_BY_OPERATOR,

        /**
         * Means that a visitor has closed the chat.
         * From this state a chat can be turned into:
         * <ul>
         * <li>{@link ChatState#NONE}, if the chat is also closed by an operator or automatically during long-term absence of activity</li>
         * <li>{@link ChatState#QUEUE}, if a visitor sends a new message ({@link MessageStream#sendMessage(String)})</li>
         * </ul>
         */
        CLOSED_BY_VISITOR,

        /**
         * The final state in which the chat can go.
         */
        DELETED,

        /**
         * Means that a chat has been started by an operator and at this moment is waiting for a visitor's response.
         * From this state a chat can be turned into:
         * <ul>
         * <li>{@link ChatState#CHATTING}, if a visitor sends a message ({@link MessageStream#sendMessage(String)})</li>
         * <li>{@link ChatState#NONE}, if an operator or a visitor closes the chat ({@link MessageStream#closeChat()})</li>
         * </ul>
         */
        INVITATION,

        /**
         * Means that the chat was sent to the router (a service that determines which operator or department
         * should service the chat), which will send it to the queue.
         * From this state a chat can be turned into:
         * <ul>
         * <li>{@link ChatState#QUEUE}, if a visitor sends a new message ({@link MessageStream#sendMessage(String)})</li>
         * </ul>
         */
        ROUTING,

        /**
         * Means the absence of a chat as such, i.e. a chat has not been started by a visitor nor by an operator.
         * From this state a chat can be turned into:
         * <ul>
         * <li>{@link ChatState#QUEUE}, if the chat is started by a visitor (by the first message or by calling{@link MessageStream#startChat()})</li>
         * <li>{@link ChatState#INVITATION}, if the chat is started by an operator</li>
         * </ul>
         */
        NONE,

        /**
         * Means that a chat has been started by a visitor and at this moment is being in the queue for processing by an operator.
         * From this state a chat can be turned into:
         * <ul>
         * <li>{@link ChatState#CHATTING}, if an operator takes the chat for processing</li>
         * <li>{@link ChatState#NONE}, if a visitor closes the chat (by calling {@link MessageStream#closeChat()}) before
         * it is taken for processing</li>
         * <li>{@link ChatState#CLOSED_BY_OPERATOR}, if an operator closes the chat without taking it for processing</li>
         * </ul>
         */
        QUEUE,

        /**
         * The state is undefined. This state is set as the initial when creating a new session, until the first response of the server containing the
         * actual state is got. This state is also used as a fallback if SDK can not identify
         * the server state (for instance, if the server has been updated to a version that contains new states).
         */
        UNKNOWN
    }

    /**
     * User can send online or offline messages. Online status determines which messages can be
     * sent by user.
     * This status depends on operator online status.
     */
    enum OnlineStatus {
        /**
         * Session has not received data from server.
         */
        UNKNOWN,

        /**
         * User can send online and offline messages.
         */
        ONLINE,

        /**
         * User can send offline messages, but server can return error.
         */
        BUSY_ONLINE,

        /**
         * User can send offline messages.
         */
        OFFLINE,

        /**
         * User can't send messages.
         */
        BUSY_OFFLINE,
    }

    /**
     * Session possible states.
     * @see MessageStream#getVisitSessionState()
     * @see VisitSessionStateListener
     */
    enum VisitSessionState {
        /**
         * Chat in progress.
         */
        CHAT,
        /**
         * Chat must be started with department selected (there was a try to start chat without
         * department selected).
         */
        DEPARTMENT_SELECTION,
        /**
         * Session is active but no chat is occurring (chat was not started yet).
         */
        IDLE,
        /**
         * Session is active but no chat is occurring (chat was closed recently).
         */
        IDLE_AFTER_CHAT,
        /**
         * Offline state.
         */
        OFFLINE_MESSAGE,
        /**
         * First status is not received yet or status is not supported by this version of the
         * library.
         */
        UNKNOWN
    }

    /**
     * Provides methods to track changes of {@link VisitSessionState} status.
     * @see VisitSessionState
     */
    interface VisitSessionStateListener {
        /**
         * Called when {@link VisitSessionState} status is changed.
         * @see VisitSessionState
         * @param previousState previous value of {@link VisitSessionState} status
         * @param newState new value of {@link VisitSessionState} status
         */
        void onStateChange(@NonNull VisitSessionState previousState,
                           @NonNull VisitSessionState newState);
    }

    /**
     * @see MessageStream#setChatStateListener
     * @see MessageStream#getChatState()
     */
    interface ChatStateListener {
        /**
         * Called during {@link ChatState} transition
         * @param oldState the previous state
         * @param newState the new state
         */
        void onStateChange(@NonNull ChatState oldState, @NonNull ChatState newState);
    }

    /**
     * @see MessageStream#setCurrentOperatorChangeListener
     * @see MessageStream#getCurrentOperator()
     */
    interface CurrentOperatorChangeListener {
        /**
         * Called when {@link Operator} of the current chat changed
         * @param oldOperator the previous operator
         * @param newOperator the new operator
         */
        void onOperatorChanged(@Nullable Operator oldOperator, @Nullable Operator newOperator);
    }

    /**
     * @see MessageStream#setOperatorTypingListener
     */
    interface OperatorTypingListener {
        /**
         * Called when operator typing state changed
         * @param isTyping true if operator is typing, false otherwise
         */
        void onOperatorTypingStateChanged(boolean isTyping);
    }

    /**
     * Provides methods to track changes in departments list.
     * @see Department
     */
    interface DepartmentListChangeListener {
        /**
         * Called when department list is received.
         * @see Department
         * @param departmentList received (current) department list
         */
        void receivedDepartmentList(@NonNull List<Department> departmentList);
    }

    /**
     * Interface that provides methods for handling MessageStream LocationSettings which are received from server.
     */
    interface LocationSettings {

        /**
         * This method shows to an app if it should show hint questions to visitor.
         * @return true if an app should show hint questions to visitor, false otherwise
         */
        boolean areHintsEnabled();

    }

    /**
     * Interface that provides methods for handling changes in MessageStream LocationSettings.
     */
    interface LocationSettingsChangeListener {

        /**
         * Method called by an app when new MessageStream LocationSettings received.
         * @param oldLocationSettings previous LocationSettings
         * @param newLocationSettings new LocationSettings
         */
        void onLocationSettingsChanged(LocationSettings oldLocationSettings, LocationSettings newLocationSettings);

    }

    /**
     * Interface that provides methods for handling changes of online status.
     */
    interface OnlineStatusChangeListener {

        /**
         * Called when online status changed.
         * @param oldOnlineStatus previous online status
         * @param newOnlineStatus new online status
         */
        void onOnlineStatusChanged(
                OnlineStatus oldOnlineStatus,
                OnlineStatus newOnlineStatus
        );
    }

    /**
     * Interface that provides methods for handling changes of parameter that is to be returned by
     * {@link MessageStream#getUnreadByOperatorTimestamp()} method.
     * @see MessageStream#setUnreadByOperatorTimestampChangeListener(UnreadByOperatorTimestampChangeListener)
     */
    interface UnreadByOperatorTimestampChangeListener {

        /***
         * Method to be called when parameter that is to be returned by
         * {@link MessageStream#getUnreadByOperatorTimestamp()} method is changed.
         * @param newTimestamp new parameter value
         */
        void onUnreadByOperatorTimestampChanged(@Nullable Date newTimestamp);

    }

    /**
     * Interface that provides methods for handling changes of parameter that is to be returned by
     * {@link MessageStream#getUnreadByVisitorTimestamp()} method.
     * @see MessageStream#setUnreadByVisitorTimestampChangeListener(UnreadByVisitorTimestampChangeListener)
     */
    interface UnreadByVisitorTimestampChangeListener {

        /***
         * Method to be called when parameter that is to be returned by
         * {@link MessageStream#getUnreadByVisitorTimestamp()} method is changed.
         * @param newTimestamp new parameter value
         */
        void onUnreadByVisitorTimestampChanged(@Nullable Date newTimestamp);

    }

    /**
     * Interface that provides methods for handling changes of parameter that is to be returned by
     * {@link MessageStream#getUnreadByVisitorMessageCount()} method.
     * @see MessageStream#setUnreadByVisitorMessageCountChangeListener(UnreadByVisitorMessageCountChangeListener)
     */
    interface UnreadByVisitorMessageCountChangeListener {

        /***
         * Method to be called when parameter that is to be returned by
         * {@link MessageStream#getUnreadByVisitorMessageCount()} method is changed.
         * @param newMessageCount new parameter value
         */
        void onUnreadByVisitorMessageCountChanged(int newMessageCount);

    }

    /**
     * @see MessageStream#setSurveyListener(SurveyListener)
     */
    interface SurveyListener {

        /**
         * This method is called one time when new survey was sent by server.
         * @param survey survey that was sent
         */
        void onSurvey(Survey survey);

        /**
         * This method provide next question in  the survey. It is called in one of two ways:
         * survey first received or answer for previous question was successfully sent to server.
         * @param question next question in the survey
         * @see MessageStream#sendSurveyAnswer(String, MessageStream.SurveyAnswerCallback)
         */
        void onNextQuestion(Survey.Question question);

        /**
         * This method is called when survey timeout expires on server. It means that survey
         * deletes and you can no longer send an answer to the question.
         */
        void onSurveyCancelled();
    }

    /**
     * @see MessageStream#sendDialogToEmailAddress(String, SendDialogToEmailAddressCallback)
     */
    interface SendDialogToEmailAddressCallback {

        /**
         * Invoked if sending succeed.
         */
        void onSuccess();

        /**
         * Invoked if sending failure.
         * @param error Error
         * @see SendDialogToEmailAddressError
         */
        void onFailure(@NonNull WebimError<SendDialogToEmailAddressError> error);

        /**
         * @see SendDialogToEmailAddressCallback#onFailure
         */
        enum SendDialogToEmailAddressError {

            /**
             * There is no chat to send it to the mail
             */
            NO_CHAT,

            /**
             * Exceeded sending attempts
             */
            SENT_TOO_MANY_TIMES,

            /**
             * An unexpected error occurred while sending
             */
            UNKNOWN
        }
    }

    /**
     * @see MessageStream#sendSticker(int, SendStickerCallback)
     */
    interface SendStickerCallback {

        /**
         * Invoked if sending succeed.
         */
        void onSuccess();

        /**
         * Invoked if sending failure.
         * @param sendStickerErrorWebimError Error
         * @see SendStickerError
         */
        void onFailure(@NonNull WebimError<SendStickerError> sendStickerErrorWebimError);

        /**
         * @see SendStickerCallback#onFailure
         */
        enum SendStickerError {

            /**
             * There is no chat to send it to the sticker
             */
            NO_CHAT,

            /**
             * Not set sticker id
             */
            NO_STICKER_ID
        }
    }
}