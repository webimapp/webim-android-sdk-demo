package ru.webim.android.sdk.impl.backend;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import ru.webim.android.sdk.MessageStream;
import ru.webim.android.sdk.NotFatalErrorHandler.NotFatalErrorType;
import ru.webim.android.sdk.WebimSession;
import ru.webim.android.sdk.impl.WebimErrorImpl;
import ru.webim.android.sdk.impl.items.responses.DefaultResponse;
import ru.webim.android.sdk.impl.items.responses.HistoryBeforeResponse;
import ru.webim.android.sdk.impl.items.responses.HistorySinceResponse;
import ru.webim.android.sdk.impl.items.responses.LocationSettingsResponse;
import ru.webim.android.sdk.impl.items.responses.LocationStatusResponse;
import ru.webim.android.sdk.impl.items.responses.SearchResponse;
import ru.webim.android.sdk.impl.items.responses.UploadResponse;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class WebimActionsImpl implements WebimActions {
    private static final MediaType PLAIN_TEXT = MediaType.parse("text/plain");
    private static final RequestBody CHAT_MODE_ONLINE = RequestBody.create(PLAIN_TEXT, "online");
    private static final String ACTION_CHAT_CLOSE = "chat.close";
    private static final String ACTION_CHAT_CLEAR_HISTORY = "chat.clear_history";
    private static final String ACTION_CHAT_KEYBOARD_RESPONSE = "chat.keyboard_response";
    private static final String ACTION_CHAT_READ_BY_VISITOR = "chat.read_by_visitor";
    private static final String ACTION_CHAT_START = "chat.start";
    private static final String ACTION_CHAT_MESSAGE = "chat.message";
    private static final String ACTION_CHAT_DELETE_MESSAGE = "chat.delete_message";
    private static final String ACTION_OPERATOR_RATE = "chat.operator_rate_select";
    private static final String ACTION_PUSH_TOKEN_SET = "set_push_token";
    private static final String ACTION_REQUEST_CALL_SENTRY
            = "chat.action_request.call_sentry_action_request";
    private static final String ACTION_SEND_CHAT_HISTORY = "chat.send_chat_history";
    private static final String ACTION_REACT_MESSAGE = "chat.react_message";
    private static final String ACTION_SEND_STICKER = "sticker";
    private static final String ACTION_SET_PRECHAT_FIELDS = "chat.set_prechat_fields";
    private static final String ACTION_VISITOR_TYPING = "chat.visitor_typing";
    private static final String ACTION_WIDGET_UPDATE = "widget.update";
    private static final String ACTION_SURVEY_ANSWER = "survey.answer";
    private static final String ACTION_SURVEY_CANCEL = "survey.cancel";
    private static final String CHARACTERS_TO_ENCODE = "\n!#$&'()*+,/:;=?@[] \"%-.<>\\^_`{|}~";
    @NonNull
    private final ActionRequestLoop requestLoop;
    @NonNull
    private final ActionRequestLoop pollerLoop;
    @NonNull
    private final WebimService webim;

    WebimActionsImpl(@NonNull WebimService webim, @NonNull ActionRequestLoop requestLoop, @NonNull ActionRequestLoop pollerLoop) {
        this.webim = webim;
        this.requestLoop = requestLoop;
        this.pollerLoop = pollerLoop;
    }

    private void enqueueRequestLoop(ActionRequestLoop.WebimRequest<?> request) {
        requestLoop.enqueue(request);
    }

    private void enqueuePollerLoop(ActionRequestLoop.WebimRequest<?> request) {
        pollerLoop.enqueue(request);
    }

    @Override
    public void sendMessage(@NonNull final String message,
                            @NonNull final String clientSideId,
                            @Nullable final String dataJsonString,
                            final boolean isHintQuestion,
                            @Nullable final SendOrDeleteMessageInternalCallback callback) {
        message.getClass(); // NPE
        clientSideId.getClass(); // NPE

        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>((callback != null)) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                /*
                Custom percent encoding for message because Retrofit/OkHTTP don't encode
                semicolons.
                */
                return webim.sendMessage(
                        ACTION_CHAT_MESSAGE,
                        percentEncode(message),
                        null,
                        clientSideId,
                        authData.getPageId(),
                        authData.getAuthToken(),
                        (isHintQuestion ? true : null),
                        dataJsonString);
            }

            @Override
            public void runCallback(DefaultResponse response) {
                //noinspection ConstantConditions
                callback.onSuccess("");
            }

            @Override
            public boolean isHandleError(@NonNull String error) {
                return true;
            }

            @Override
            public void handleError(@NonNull String error) {
                //noinspection ConstantConditions
                callback.onFailure(error);
            }
        });
    }

    @Override
    public void sendFiles(@NonNull final String message,
                          @NonNull final String clientSideId,
                          final boolean isHintQuestion,
                          @Nullable final SendOrDeleteMessageInternalCallback callback) {

        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>((callback != null)) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                /*
                Custom percent encoding for message because Retrofit/OkHTTP don't encode
                semicolons.
                */
                return webim.sendMessage(
                        ACTION_CHAT_MESSAGE,
                        percentEncode(message),
                        "file_visitor",
                        clientSideId,
                        authData.getPageId(),
                        authData.getAuthToken(),
                        (isHintQuestion ? true : null),
                        null);
            }

            @Override
            public void runCallback(DefaultResponse response) {
                //noinspection ConstantConditions
                callback.onSuccess("");
            }

            @Override
            public boolean isHandleError(@NonNull String error) {
                return true;
            }

            @Override
            public void handleError(@NonNull String error) {
                //noinspection ConstantConditions
                callback.onFailure(error);
            }
        });
    }

    @Override
    public void sendKeyboard(@NonNull final String requestMessageId,
                             @NonNull final String buttonId,
                             @Nullable final SendKeyboardErrorListener callback) {
        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>(callback != null) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.sendKeyboardResponse(
                        authData.getPageId(),
                        authData.getAuthToken(),
                        ACTION_CHAT_KEYBOARD_RESPONSE,
                        requestMessageId,
                        buttonId);
            }

            @Override
            public void runCallback(DefaultResponse response) {
                //noinspection ConstantConditions
                callback.onSuccess();
            }

            @Override
            public boolean isHandleError(@NonNull String error) {
                return true;
            }

            @Override
            public void handleError(@NonNull String error) {
                //noinspection ConstantConditions
                callback.onFailure(error);
            }
        });
    }

    @Override
    public void deleteMessage(@NonNull final String clientSideId,
                              @Nullable final SendOrDeleteMessageInternalCallback callback) {
        clientSideId.getClass(); // NPE

        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>(callback != null) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.deleteMessage(
                        ACTION_CHAT_DELETE_MESSAGE,
                        clientSideId,
                        authData.getPageId(),
                        authData.getAuthToken());
            }

            @Override
            public void runCallback(DefaultResponse response) {
                //noinspection ConstantConditions
                callback.onSuccess("");
            }

            @Override
            public boolean isHandleError(@NonNull String error) {
                return true;
            }

            @Override
            public void handleError(@NonNull String error) {
                //noinspection ConstantConditions
                callback.onFailure(error);
            }
        });
    }

    @Override
    public void replyMessage(@NonNull final String message,
                             @NonNull final String clientSideId,
                             @NonNull final String quoteMessageId) {
        message.getClass(); // NPE
        clientSideId.getClass(); // NPE

        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>(true) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                /*
                Custom percent encoding for message because Retrofit/OkHTTP don't encode
                semicolons.
                */
                return webim.replyMessage(
                        ACTION_CHAT_MESSAGE,
                        percentEncode(message),
                        clientSideId,
                        getReferenceToMessage(quoteMessageId),
                        authData.getPageId(),
                        authData.getAuthToken());
            }
        });
    }

    @Override
    public void sendFile(final @NonNull RequestBody body,
                         final @NonNull String filename,
                         final @NonNull String clientSideId,
                         final @Nullable SendOrDeleteMessageInternalCallback callback) {
        body.getClass(); // NPE
        filename.getClass(); // NPE
        clientSideId.getClass(); // NPE

        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<UploadResponse>((callback != null)) {
            @Override
            public Call<UploadResponse> makeRequest(AuthData authData) {
                return webim.uploadFile(
                        MultipartBody.Part.createFormData(
                                "webim_upload_file",
                                filename,
                                body),
                        CHAT_MODE_ONLINE,
                        RequestBody.create(PLAIN_TEXT, clientSideId),
                        RequestBody.create(PLAIN_TEXT, authData.getPageId()),
                        ((authData.getAuthToken() == null)
                                ? null
                                : RequestBody.create(PLAIN_TEXT, authData.getAuthToken()))
                );
            }

            @Override
            public void runCallback(UploadResponse response) {
                //noinspection ConstantConditions
                callback.onSuccess(response.getData().toString());
            }

            @Override
            public boolean isHandleError(@NonNull String error) {
                return true;
            }

            @Override
            public void handleError(@NonNull String error) {
                //noinspection ConstantConditions
                callback.onFailure(error);
            }
        });
    }

    @Override
    public void deleteUploadedFile(@NonNull final String fileGuid,
                                   @Nullable final SendOrDeleteMessageInternalCallback callback) {

        fileGuid.getClass();

        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>((callback != null)) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.deleteUploadedFile(
                        authData.getPageId(),
                        fileGuid,
                        authData.getAuthToken());
            }

            @Override
            public void runCallback(DefaultResponse response) {
                //noinspection ConstantConditions
                callback.onSuccess("");
            }

            @Override
            public boolean isHandleError(@NonNull String error) {
                return (error.equals(WebimInternalError.FILE_NOT_FOUND)
                        || error.equals(WebimInternalError.FILE_HAS_BEEN_SENT));
            }

            @Override
            public void handleError(@NonNull String error) {
                //noinspection ConstantConditions
                callback.onFailure(error);
            }
        });
    }

    @Override
    public void closeChat() {
        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>(false) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.closeChat(
                        ACTION_CHAT_CLOSE,
                        authData.getPageId(),
                        authData.getAuthToken());
            }
        });
    }

    @Override
    public void clearChatHistory(@NonNull DefaultCallback<DefaultResponse> callback) {
        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>(true) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.clearChatHistory(
                    ACTION_CHAT_CLEAR_HISTORY,
                    authData.getPageId(),
                    authData.getAuthToken()
                );
            }

            @Override
            public void runCallback(DefaultResponse response) {
                callback.onSuccess(response);
            }
        });
    }

    @Override
    public void startChat(@NonNull final String clientSideId,
                          @Nullable final String departmentKey,
                          @Nullable final String firstQuestion,
                          @Nullable final String customFields) {
        clientSideId.getClass(); // NPE

        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>(false) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.startChat(
                        ACTION_CHAT_START,
                        true,
                        clientSideId,
                        authData.getPageId(),
                        authData.getAuthToken(),
                        departmentKey,
                        firstQuestion,
                        customFields
                );
            }
        });
    }

    @Override
    public void searchMessages(@NonNull final String query,
                               @NonNull final DefaultCallback<SearchResponse> callback) {

        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<SearchResponse>(true) {
            @Override
            public Call<SearchResponse> makeRequest(AuthData authData) {
                return webim.searchMessages(query, authData.getPageId(), authData.getAuthToken());
            }

            @Override
            public void runCallback(SearchResponse response) {
                callback.onSuccess(response);
            }

            @Override
            public boolean isHandleError(@NonNull String error) {
                return false;
            }
        });
    }

    @Override
    public void setChatRead() {
        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>(false) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.setChatRead(
                        ACTION_CHAT_READ_BY_VISITOR,
                        authData.getPageId(),
                        authData.getAuthToken()
                );
            }
        });
    }

    @Override
    public void reactMessage(@NonNull String clientSideId, @NonNull String reaction, SendOrDeleteMessageInternalCallback callback) {
        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>(true) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.reactMessage(
                    ACTION_REACT_MESSAGE,
                    clientSideId,
                    reaction,
                    authData.getPageId(),
                    authData.getAuthToken());
            }

            @Override
            public boolean isHandleError(@NonNull String error) {
                return true;
            }

            @Override
            public void handleError(@NonNull String error) {
                callback.onFailure(error);
            }

            @Override
            public void runCallback(DefaultResponse response) {
                callback.onSuccess(response.getResult());
            }
        });
    }

    @Override
    public void setPrechatFields(@NonNull final String prechatFields) {
        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>(false) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.setPrechatFields(
                        ACTION_SET_PRECHAT_FIELDS,
                        prechatFields,
                        authData.getPageId(),
                        authData.getAuthToken());
            }
        });
    }

    @Override
    public void setVisitorTyping(final boolean typing,
                                 final @Nullable String draftMessage,
                                 final boolean deleteDraft) {
        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>(false) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.setVisitorTyping(
                        ACTION_VISITOR_TYPING,
                        typing,
                        draftMessage,
                        deleteDraft,
                        authData.getPageId(),
                        authData.getAuthToken());
            }
        });
    }

    public void updatePushToken(@NonNull final String pushToken,
                                @Nullable final WebimSession.TokenCallback callback) {
        pushToken.getClass(); // NPE
        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>(callback != null) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.updatePushToken(
                        ACTION_PUSH_TOKEN_SET,
                        pushToken,
                        authData.getPageId(),
                        authData.getAuthToken());
            }

            @Override
            public void runCallback(DefaultResponse response) {
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public boolean isHandleError(@NonNull String error) {
                return callback != null;
            }

            @Override
            public void handleError(@NonNull String error) {
                if (callback != null) {
                    WebimSession.TokenCallback.TokenError tokenError;
                    if (error.equals(NotFatalErrorType.SOCKET_TIMEOUT_EXPIRED.toString())) {
                        tokenError = WebimSession.TokenCallback.TokenError.SOCKET_TIMEOUT_EXPIRED;
                    } else {
                        tokenError = WebimSession.TokenCallback.TokenError.UNKNOWN;
                    }
                    callback.onFailure(new WebimErrorImpl<>(tokenError, error));
                }
            }
        });
    }

    @Override
    public void rateOperator
            (@Nullable final String operatorId,
             @Nullable final String note,
             final int rate,
             @Nullable final MessageStream.RateOperatorCallback rateOperatorCallback) {
        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>(
                (rateOperatorCallback != null)
        ) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.rateOperator(
                        ACTION_OPERATOR_RATE,
                        operatorId,
                        note,
                        rate,
                        authData.getPageId(),
                        authData.getAuthToken());
            }

            @Override
            public void runCallback(DefaultResponse response) {
                if (rateOperatorCallback != null) {
                    rateOperatorCallback.onSuccess();
                }
            }

            @Override
            public boolean isHandleError(@NonNull String error) {
                return error.equals(WebimInternalError.OPERATOR_NOT_IN_CHAT)
                        || error.equals(WebimInternalError.NO_CHAT)
                        || error.equals(WebimInternalError.NOTE_IS_TOO_LONG);
            }

            @Override
            public void handleError(@NonNull String error) {
                if (rateOperatorCallback != null) {
                    MessageStream.RateOperatorCallback.RateOperatorError rateOperatorError;
                    switch (error) {
                        case WebimInternalError.NO_CHAT:
                            rateOperatorError = MessageStream.RateOperatorCallback.RateOperatorError.NO_CHAT;
                            break;
                        case WebimInternalError.NOTE_IS_TOO_LONG:
                            rateOperatorError = MessageStream.RateOperatorCallback.RateOperatorError.NOTE_IS_TOO_LONG;
                            break;
                        default:
                            rateOperatorError = MessageStream.RateOperatorCallback.RateOperatorError.OPERATOR_NOT_IN_CHAT;
                    }
                    rateOperatorCallback.onFailure((new WebimErrorImpl<>(rateOperatorError, error)));
                }
            }
        });
    }

    @Override
    public void respondSentryCall(@NonNull final String clientSideId) {
        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>(false) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.respondSentryCall(
                        ACTION_REQUEST_CALL_SENTRY,
                        authData.getPageId(),
                        authData.getAuthToken(),
                        clientSideId);
            }
        });
    }

    @Override
    public void requestHistoryBefore
            (final long beforeTs,
             @NonNull final DefaultCallback<HistoryBeforeResponse> callback) {
        callback.getClass(); // NPE
        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<HistoryBeforeResponse>(true) {
            @Override
            public Call<HistoryBeforeResponse> makeRequest(AuthData authData) {
                return webim.getHistoryBefore(authData.getPageId(),
                        authData.getAuthToken(), beforeTs);
            }

            @Override
            public void runCallback(HistoryBeforeResponse response) {
                callback.onSuccess(response);
            }
        });
    }

    @Override
    public void requestHistorySince(final @Nullable String since,
                                    @NonNull final DefaultCallback<HistorySinceResponse> callback) {
        callback.getClass(); // NPE
        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<HistorySinceResponse>(true) {
            @Override
            public Call<HistorySinceResponse> makeRequest(AuthData authData) {
                return webim.getHistorySince(authData.getPageId(), authData.getAuthToken(), since);
            }

            @Override
            public void runCallback(HistorySinceResponse response) {
                callback.onSuccess(response);
            }
        });
    }

    @Override
    public void requestHistorySinceForPoller(final @Nullable String since,
                                    @NonNull final DefaultCallback<HistorySinceResponse> callback) {
        callback.getClass(); // NPE
        enqueuePollerLoop(new ActionRequestLoop.WebimRequest<HistorySinceResponse>(true) {
            @Override
            public Call<HistorySinceResponse> makeRequest(AuthData authData) {
                return webim.getHistorySince(authData.getPageId(), authData.getAuthToken(), since);
            }

            @Override
            public void runCallback(HistorySinceResponse response) {
                callback.onSuccess(response);
            }
        });
    }

    @Override
    public void updateWidgetStatus(@NonNull final String data) {
        data.getClass(); //NPE
        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>(false) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.updateWidgetStatus(
                        ACTION_WIDGET_UPDATE,
                        data,
                        authData.getPageId(),
                        authData.getAuthToken()
                );
            }
        });
    }

    @Override
    public void sendChatToEmailAddress(@NonNull final String email,
                                       @NonNull final MessageStream.SendDialogToEmailAddressCallback sendChatToEmailCallback) {
        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>(true) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.sendChatHistory(
                        ACTION_SEND_CHAT_HISTORY,
                        email,
                        authData.getPageId(),
                        authData.getAuthToken()
                );
            }

            @Override
            public void runCallback(DefaultResponse response) {
                sendChatToEmailCallback.onSuccess();
            }

            @Override
            public boolean isHandleError(@NonNull String error) {
                return error.equals(WebimInternalError.SENT_TOO_MANY_TIMES);
            }

            @Override
            public void handleError(@NonNull String error) {
                MessageStream.SendDialogToEmailAddressCallback.SendDialogToEmailAddressError sendDialogToEmailAddressError;
                sendDialogToEmailAddressError = WebimInternalError.SENT_TOO_MANY_TIMES.equals(error)
                        ? MessageStream.SendDialogToEmailAddressCallback.SendDialogToEmailAddressError.SENT_TOO_MANY_TIMES
                        : MessageStream.SendDialogToEmailAddressCallback.SendDialogToEmailAddressError.UNKNOWN;
                sendChatToEmailCallback.onFailure(new WebimErrorImpl<>(sendDialogToEmailAddressError, error));
            }
        });
    }

    @Override
    public void sendSticker(final int stickerId,
                            @NonNull final String clientSideId,
                            @Nullable final MessageStream.SendStickerCallback sendStickerCallback) {

        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>((sendStickerCallback != null)) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.sendSticker(
                        ACTION_SEND_STICKER,
                        stickerId,
                        clientSideId,
                        authData.getPageId(),
                        authData.getAuthToken());
            }

            @Override
            public void runCallback(DefaultResponse response) {
                if (sendStickerCallback != null) {
                    sendStickerCallback.onSuccess();
                }
            }

            @Override
            public boolean isHandleError(@NonNull String error) {
                return error.equals(WebimInternalError.NO_CHAT)
                        || error.equals(WebimInternalError.NO_STICKER_ID);
            }

            @Override
            public void handleError(@NonNull String error) {
                if (sendStickerCallback != null) {
                    MessageStream.SendStickerCallback.SendStickerError sendStickerError;
                    switch (error) {
                        case WebimInternalError.NO_CHAT:
                            sendStickerError = MessageStream.SendStickerCallback.SendStickerError.NO_CHAT;
                            break;
                        case WebimInternalError.NO_STICKER_ID:
                        default:
                            sendStickerError = MessageStream.SendStickerCallback.SendStickerError.NO_STICKER_ID;
                    }
                    sendStickerCallback.onFailure((new WebimErrorImpl<>(sendStickerError, error)));
                }
            }
        });
    }

    @Override
    public void sendQuestionAnswer(@NonNull final String surveyId,
                                   final int formId,
                                   final int questionId,
                                   @NonNull final String surveyAnswer,
                                   @Nullable final SurveyQuestionCallback callback) {
        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>(callback != null) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.sendSurveyAnswer(
                    ACTION_SURVEY_ANSWER,
                        formId,
                        questionId,
                        surveyId,
                        surveyAnswer,
                        authData.getPageId(),
                        authData.getAuthToken()
                );
            }

            @Override
            public void runCallback(DefaultResponse response) {
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public boolean isHandleError(@NonNull String error) {
                return callback != null;
            }

            @Override
            public void handleError(@NonNull String error) {
                if (callback != null) {
                    callback.onFailure(error);
                }
            }
        });
    }

    @Override
    public void closeSurvey(@NonNull final String surveyId, @NonNull final SurveyFinishCallback callback) {
        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<DefaultResponse>(true) {
            @Override
            public Call<DefaultResponse> makeRequest(AuthData authData) {
                return webim.closeSurvey(
                    ACTION_SURVEY_CANCEL,
                    surveyId,
                    authData.getPageId(),
                    authData.getAuthToken()
                );
            }

            @Override
            public void runCallback(DefaultResponse response) {
                callback.onSuccess();
            }

            @Override
            public boolean isHandleError(@NonNull String error) {
                return true;
            }

            @Override
            public void handleError(@NonNull String error) {
                callback.onFailure(error);
            }
        });
    }

    @Override
    public void getLocationStatus(@NonNull String location, @NonNull DefaultCallback<LocationStatusResponse> callback) {
        enqueuePollerLoop(new ActionRequestLoop.WebimRequest<LocationStatusResponse>(true) {

            @Override
            public Call<LocationStatusResponse> makeRequest(AuthData authData) {
                return webim.getOnlineStatus(location);
            }

            @Override
            public void runCallback(LocationStatusResponse response) {
                callback.onSuccess(response);
            }
        });
    }

    @Override
    public void getLocationConfig(@NonNull String location, @NonNull DefaultCallback<LocationSettingsResponse> callback) {
        enqueueRequestLoop(new ActionRequestLoop.WebimRequest<LocationSettingsResponse>(true) {

            @Override
            public Call<LocationSettingsResponse> makeRequest(AuthData authData) {
                return webim.getAccountConfig(location);
            }

            @Override
            public void runCallback(LocationSettingsResponse response) {
                callback.onSuccess(response);
            }

            @Override
            public boolean isHandleError(@NotNull String error) {
                return true;
            }

            @Override
            public void handleError(@NotNull String error) {
                callback.onSuccess(null);
            }
        });
    }

    private static String percentEncode(String input) {
        if ((input == null) || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder(input);
        for (int i = (input.length() - 1); i >= 0; i--) {
            if (CHARACTERS_TO_ENCODE.indexOf(input.charAt(i)) != -1) {
                result.replace(
                        i,
                        (i + 1),
                        ("%" + Integer.toHexString(0x100 | input.charAt(i))
                                .substring(1).toUpperCase())
                );
            }
        }

        return result.toString();
    }

    private String getReferenceToMessage(@NonNull String quotedMessage) {
        return "{\"ref\":{" +
                    "\"msgId\":\"" + quotedMessage + "\"," +
                    "\"msgChannelSideId\":null," +
                    "\"chatId\":null" +
                    "}" +
                "}";
    }
}
