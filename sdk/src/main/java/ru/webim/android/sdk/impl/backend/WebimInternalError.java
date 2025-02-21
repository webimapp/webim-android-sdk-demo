package ru.webim.android.sdk.impl.backend;

public class WebimInternalError {
	public static final String ACCOUNT_BLOCKED = "account-blocked";
	public static final String CHAT_REQUIRED = "chat-required";
	public static final String CONNECTION_TIMEOUT = "connection-timeout";
	public static final String CONTENT_TYPE_NOT_RECOGNIZED = "content_type_not_recognized";
	public static final String DOMAIN_NOT_FROM_WHITELIST = "domain-not-from-whitelist";
	public static final String FILE_NAME_INCORRECT = "incorrect-file-name";
	public static final String FILE_NOT_FOUND = "file_not_found";
	public static final String FILE_SIZE_EXCEEDED = "max_file_size_exceeded";
	public static final String FILE_SIZE_TOO_SMALL = "file_size_too_small";
	public static final String FILE_TYPE_NOT_ALLOWED = "not_allowed_file_type";
	public static final String NOT_ALLOWED_MIME_TYPE = "not_allowed_mime_type";
	public static final String NO_PREVIOUS_CHATS = "no_previous_chats";
	public static final String NO_STICKER_ID = "no-sticker-id";
	public static final String NOT_MATCHING_MAGIC_NUMBERS = "not_matching_magic_numbers";
	public static final String MAX_FILES_COUNT_PER_CHAT_EXCEEDED = "max_files_count_per_chat_exceeded";
	public static final String PROVIDED_VISITOR_EXPIRED = "provided-visitor-expired";
	public static final String REINIT_REQUIRED = "reinit-required";
	public static final String SETTING_DISABLED = "setting_disabled";
	public static final String SERVER_NOT_READY = "server-not-ready";
	public static final String SESSION_NOT_FOUND = "session_not_found";
	public static final String UNAUTHORIZED = "unauthorized";
	public static final String UPLOADED_FILE_NOT_FOUND = "uploaded-file-not-found";
	public static final String VISITOR_BANNED = "visitor_banned";
	public static final String WRONG_ARGUMENT_VALUE = "wrong-argument-value";
	public static final String WRONG_PROVIDED_VISITOR_HASH = "wrong-provided-visitor-hash-value";

    // Data errors.
    // Quoting message error.
    public static final String QUOTED_MESSAGE_CANNOT_BE_REPLIED
            = "quoting-message-that-cannot-be-replied";
    public static final String QUOTED_MESSAGE_FROM_ANOTHER_VISITOR
            = "quoting-message-from-another-visitor";
    public static final String QUOTED_MESSAGE_CORRUPTED_ID = "corrupted-quoted-message-id";
    public static final String QUOTED_MESSAGE_MULTIPLE_IDS = "multiple-quoted-messages-found";
    public static final String QUOTED_MESSAGE_NOT_FOUND = "quoted-message-not-found";
    public static final String QUOTED_MESSAGE_REQUIRED_ARGUMENTS_MISSING
            = "required-quote-args-missing";

    // Provided authorization token errors.
    public static final String PROVIDED_AUTHORIZATION_TOKEN_NOT_FOUND
            = "provided-auth-token-not-found";

    // Send, edit and delete message errors.
	// send or edit:
	public static final String MESSAGE_EMPTY = "message_empty";
	public static final String MAX_MESSAGE_LENGTH_EXCEEDED = "max-message-length-exceeded";
	// delete:
	public static final String MESSAGE_NOT_FOUND = "message_not_found";
	// edit or delete
	public static final String NOT_ALLOWED = "not_allowed";
	public static final String MESSAGE_NOT_OWNED = "message_not_owned";
	// edit
	public static final String WRONG_MESSAGE_KIND = "wrong_message_kind";

    // Rate operator errors.
	public static final String NO_CHAT = "no-chat";
	public static final String OPERATOR_NOT_IN_CHAT = "operator-not-in-chat";
	public static final String OPERATOR_ALREADY_RATED = "operator-already-rated";
	public static final String NOTE_IS_TOO_LONG = "note-is-too-long";

	//Errors for sending keyboard
	public static final String BUTTON_ID_NO_SET = "button-id-not-set";
	public static final String REQUEST_MESSAGE_ID_NOT_SET = "request-message-id-not-set";
	public static final String CANNOT_CREATE_RESPONSE = "cannot-create-response";

	//Errors for sending chat on email
	public  static final String SENT_TOO_MANY_TIMES = "chat-history-sent-too-many-times";

	//Survey errors
	public static final String SURVEY_DISABLED = "survey-disabled";
	public static final String NO_CURRENT_SURVEY = "no-current-survey";
	public static final String INCORRECT_SURVEY_ID = "incorrect-survey-id";
	public static final String INCORRECT_STARS_VALUE = "incorrect-stars-value";
	public static final String INCORRECT_RADIO_VALUE = "incorrect-radio-value";
	public static final String MAX_COMMENT_LENGTH_EXCEEDED = "max-comment-length-exceeded";
	public static final String QUESTION_NOT_FOUND = "question-not-found";

	//Errors for Delete files
	public static final String FILE_HAS_BEEN_SENT = "file-has-been-sent";

	//Geo errors
	public static final String INVALID_COORDINATES = "invalid-coordinates-received";
}
