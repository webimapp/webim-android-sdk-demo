package ru.webim.android.sdk.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.webim.android.sdk.Message;
import ru.webim.android.sdk.Operator;

import java.util.List;

public class MessageImpl implements Message, TimeMicrosHolder, Comparable<MessageImpl> {

    protected final @Nullable String avatarUrl;
    protected final @NonNull Id clientSideId;
    protected final @Nullable  String sessionId;
    protected final @Nullable Keyboard keyboard;
    protected final @Nullable KeyboardRequest keyboardRequest;
    protected final @Nullable Operator.Id operatorId;
    protected final @NonNull String senderName;
    protected final @NonNull String serverUrl;
    protected final long timeMicros;
    protected final @NonNull Type type;
    protected @NonNull String text;

    protected @NonNull SendStatus sendStatus = SendStatus.SENT;

    private final @Nullable Attachment attachment;
    private final @Nullable String rawText;

    private String serverSideId;
    protected boolean savedInHistory;
    private boolean readByOperator;
    private boolean canBeEdited;
    private boolean canBeReplied;
    private boolean edited;
    private boolean canVisitorReact;
    private boolean canVisitorChangeReaction;
    private @Nullable MessageReaction reaction;
    private @Nullable Quote quote;
    private @Nullable Sticker sticker;

    public MessageImpl(
            @NonNull String serverUrl,
            @NonNull Id clientSideId,
            @Nullable String sessionId,
            @Nullable Operator.Id operatorId,
            @Nullable String avatarUrl,
            @NonNull String senderName,
            @NonNull Type type,
            @NonNull String text,
            long timeMicros,
            String serverSideId,
            @Nullable String rawText,
            boolean savedInHistory,
            @Nullable Attachment attachment,
            boolean readByOperator,
            boolean canBeEdited,
            boolean canBeReplied,
            boolean edited,
            @Nullable Quote quote,
            @Nullable Keyboard keyboard,
            @Nullable KeyboardRequest keyboardRequest,
            @Nullable Sticker sticker,
            @Nullable MessageReaction reaction,
            boolean canVisitorReact,
            boolean canVisitorChangeReaction
    ) {
        serverUrl.getClass(); // NPE
        clientSideId.getClass(); // NPE
        senderName.getClass(); // NPE
        type.getClass(); // NPE
        text.getClass(); // NPE

        this.serverUrl = serverUrl;
        this.clientSideId = clientSideId;
        this.sessionId = sessionId;
        this.operatorId = operatorId;
        this.avatarUrl = avatarUrl;
        this.senderName = senderName;
        this.type = type;
        this.text = text;
        this.timeMicros = timeMicros;
        this.serverSideId = serverSideId;
        this.rawText = rawText;
        this.savedInHistory = savedInHistory;
        this.attachment = attachment;
        this.readByOperator = readByOperator;
        this.canBeEdited = canBeEdited;
        this.canBeReplied = canBeReplied;
        this.edited = edited;
        this.quote = quote;
        this.keyboard = keyboard;
        this.keyboardRequest = keyboardRequest;
        this.sticker = sticker;
        this.reaction = reaction;
        this.canVisitorReact = canVisitorReact;
        this.canVisitorChangeReaction = canVisitorChangeReaction;
    }

    @Override
    public boolean isReadByOperator() {
        return readByOperator || type != Message.Type.VISITOR && type != Message.Type.FILE_FROM_VISITOR;
    }

    @Override
    public boolean canBeEdited() {
        return canBeEdited;
    }

    public boolean canBeReplied() {
        return canBeReplied;
    }

    @Override
    public boolean isEdited() {
        return edited;
    }

    @Nullable
    @Override
    public Quote getQuote() {
        return quote;
    }

    @Nullable
    @Override
    public MessageReaction getReaction() {
        return reaction;
    }

    @Override
    public boolean canVisitorReact() {
        return canVisitorReact;
    }

    @Override
    public boolean canVisitorChangeReaction() {
        return canVisitorChangeReaction;
    }

    @Override
    public Keyboard getKeyboard() {
        return keyboard;
    }

    @Override
    public KeyboardRequest getKeyboardRequest() {
        return keyboardRequest;
    }

    @Nullable
    @Override
    public Sticker getSticker() {
        return sticker;
    }

    public void setReadByOperator(boolean read) {
        this.readByOperator = read;
    }

    @Nullable
    public String getAvatarUrlLastPart() {
        return avatarUrl;
    }

    @Nullable
    public Attachment getAttachment() {
        return attachment;
    }

    @Override
    public boolean isSavedInHistory() {
        return savedInHistory;
    }

    @Nullable
    public String getData() {
        return rawText;
    }

    @NonNull
    public MessageSource getSource() {
        return savedInHistory ? MessageSource.HISTORY : MessageSource.CURRENT_CHAT;
    }

    @NonNull
    @Override
    public Id getClientSideId() {
        return clientSideId;
    }

    @Nullable
    @Override
    public String getSessionId() {
        return sessionId;
    }

    @NonNull
    @Override
    public String getServerSideId() {
        return serverSideId;
    }

    @Nullable
    @Override
    public Operator.Id getOperatorId() {
        return operatorId;
    }

    @Nullable
    @Override
    public String getSenderAvatarUrl() {
        return avatarUrl == null ? null : serverUrl + avatarUrl;
    }

    @NonNull
    @Override
    public String getSenderName() {
        return senderName;
    }

    @NonNull
    @Override
    public Type getType() {
        return type;
    }

    @NonNull
    @Override
    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Message && this.getClientSideId().equals(((Message) o).getClientSideId());
    }

    @Override
    public long getTime() {
        return timeMicros / 1000;
    }

    @NonNull
    @Override
    public SendStatus getSendStatus() {
        return sendStatus;
    }

    @Override
    public long getTimeMicros() {
        return timeMicros;
    }

    public void setCanBeEdited(boolean canBeEdited) {
        this.canBeEdited = canBeEdited;
    }

    public void setCanBeReplied(boolean canBeReplied) {
        this.canBeReplied = canBeReplied;
    }

    public void setSavedInHistory(boolean savedInHistory) {
        this.savedInHistory = savedInHistory;
    }

    public boolean isContentEquals(MessageImpl message) {
        return
            clientSideId.toString().equals(message.clientSideId.toString())
                && serverSideId.equals(message.serverSideId)
                && InternalUtils.equals(operatorId == null
                    ? null
                    : operatorId.toString(),
                message.operatorId == null
                    ? null
                    : message.operatorId.toString())
                && savedInHistory == message.savedInHistory
                && InternalUtils.equals(avatarUrl, message.avatarUrl)
                && senderName.equals(message.senderName)
                && type.equals(message.type)
                && text.equals(message.text)
                && timeMicros == message.timeMicros
                && InternalUtils.equals(rawText, message.rawText)
                && isReadByOperator() == message.isReadByOperator()
                && canBeReplied == message.canBeReplied
                && canBeEdited == message.canBeEdited
                && edited == message.edited;
    }

    /**
     * Because message from history has some fields that always are not actual (canBeEdited,
     * readByOperator, rating, canVisitorReact, canVisitorChangeReaction and others) so here
     * we check only some fields.
     */
    public boolean isContentEqualsForHistoryMessage(MessageImpl history) {
        return
                clientSideId.toString().equals(history.clientSideId.toString())
                && serverSideId.equals(history.serverSideId)
                && savedInHistory == history.savedInHistory
                && text.equals(history.text)
                && edited == history.edited;
    }

    public MessageImpl mergeWithHistoryMessage(MessageImpl history) {
        return new MessageImpl(
            serverUrl,
            clientSideId,
            sessionId,
            operatorId,
            avatarUrl,
            senderName,
            type,
            history.text,
            timeMicros,
            serverSideId,
            rawText,
            history.savedInHistory,
            attachment,
            readByOperator,
            canBeEdited,
            canBeReplied,
            history.edited,
            quote,
            keyboard,
            keyboardRequest,
            sticker,
            reaction,
            canVisitorReact,
            canVisitorChangeReaction
        );
    }

    @Override
    public String toString() {
        return "MessageImpl{"
                + "\nserverUrl='" + serverUrl + '\''
                + ", \nid=" + clientSideId
                + ", \noperatorId=" + operatorId
                + ", \navatarUrl='" + avatarUrl + '\''
                + ", \nsenderName='" + senderName + '\''
                + ", \ntype=" + type
                + ", \ntext='" + text + '\''
                + ", \ntimeMicros=" + timeMicros
                + ", \nattachment=" + attachment
                + ", \nrawText='" + rawText + '\''
                + ", \nisHistoryMessage=" + savedInHistory
                + ", \ncurrentChatId='" + serverSideId + '\''
                + ", \ncanBeEdited=" + canBeEdited
                + "\n}";
    }

    @Override
    public int compareTo(MessageImpl message) {
        return message != null ? InternalUtils.compare(getTimeMicros(), message.getTimeMicros()) : 1;
    }

    public static class AttachmentImpl implements Message.Attachment {
        private int downloadProgress;
        private @Nullable String errorMessage;
        private @Nullable String errorType;
        private @NonNull FileInfo fileInfo;
        private @NonNull List<FileInfo> filesInfo;
        private @NonNull AttachmentState state;

        public AttachmentImpl(int downloadProgress,
                              @Nullable String errorMessage,
                              @Nullable String errorType,
                              @NonNull FileInfo fileInfo,
                              @NonNull List<FileInfo> filesInfo,
                              @NonNull AttachmentState state) {
            this.downloadProgress = downloadProgress;
            this.errorMessage = errorMessage;
            this.errorType = errorType;
            this.fileInfo = fileInfo;
            this.filesInfo = filesInfo;
            this.state = state;
        }

        @Override
        public int getDownloadProgress() {
            return downloadProgress;
        }

        @Override
        @Nullable
        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        @Nullable
        public String getErrorType() {
            return errorType;
        }

        @Override
        @NonNull
        public FileInfo getFileInfo() {
            return fileInfo;
        }

        @Override
        @NonNull
        public List<FileInfo> getFilesInfo() { return filesInfo; }

        @Override
        @NonNull
        public AttachmentState getState() {
            return state;
        }
    }

    public static class FileInfoImpl implements Message.FileInfo {
        private final @Nullable String contentType;
        private final @NonNull String filename;
        private final @Nullable ImageInfo imageInfo;
        private final long size;
        private final @Nullable String url;
        private final @Nullable String guid;
        private final @NonNull FileUrlCreator fileUrlCreator;

        public FileInfoImpl(@Nullable String contentType,
                            @NonNull String filename,
                            @Nullable ImageInfo imageInfo,
                            long size,
                            @Nullable String url,
                            @Nullable String guid,
                            @NonNull FileUrlCreator fileUrlCreator) {

            this.contentType = contentType;
            this.filename = filename;
            this.imageInfo = imageInfo;
            this.size = size;
            this.url = url;
            this.guid = guid;
            this.fileUrlCreator = fileUrlCreator;
        }

        @Nullable
        @Override
        public String getContentType() {
            return contentType;
        }

        @NonNull
        @Override
        public String getFileName() {
            return filename;
        }

        @Nullable
        @Override
        public ImageInfo getImageInfo() {
            return imageInfo;
        }

        @Override
        public long getSize() {
            return size;
        }

        @Nullable
        String getGuid() {
            return guid;
        }

        @Nullable
        @Override
        public String getUrl() {
            if (guid != null) {
                String currentUrl = fileUrlCreator.createFileUrl(filename, guid, false);
                if (currentUrl != null) {
                    return currentUrl;
                }
            }
            return url;
        }
    }

    public static class ImageInfoImpl implements ImageInfo {
        private final @NonNull String thumbUrl;
        private final int width;
        private final int height;
        private final @NonNull String guid;
        private final @NonNull String filename;
        private final @NonNull FileUrlCreator fileUrlCreator;

        public ImageInfoImpl(
            @NonNull String thumbUrl,
            int width,
            int height,
            @NonNull String filename,
            @NonNull String guid,
            @NonNull FileUrlCreator fileUrlCreator) {
            thumbUrl.getClass(); // NPE
            this.thumbUrl = thumbUrl;
            this.width = width;
            this.height = height;
            this.filename = filename;
            this.guid = guid;
            this.fileUrlCreator = fileUrlCreator;
        }

        @NonNull
        @Override
        public String getThumbUrl() {
            if (guid != null) {
                String currentUrl = fileUrlCreator.createFileUrl(filename, guid, true);
                if (currentUrl != null) {
                    return currentUrl;
                }
            }
            return thumbUrl;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }
    }

    public enum MessageSource {
        HISTORY,
        CURRENT_CHAT;

        public boolean isHistory() {
            return this == HISTORY;
        }

        public boolean isCurrentChat() {
            return this == CURRENT_CHAT;
        }
    }

    public static class QuoteImpl implements Message.Quote {
        @Nullable
        private final FileInfo attachment;
        @Nullable
        private final String authorId;
        @Nullable
        private final String id;
        @Nullable
        private final Type type;
        @Nullable
        private final String senderName;
        @NonNull
        private final State status;
        @Nullable
        private final String text;
        private final
        long timeSeconds;

        public QuoteImpl(
                @Nullable FileInfo attachment,
                @Nullable String authorId,
                @Nullable String id,
                @Nullable Type type,
                @Nullable String senderName,
                @NonNull State status,
                @Nullable String text,
                long timeSeconds) {
            this.attachment = attachment;
            this.authorId = authorId;
            this.id = id;
            this.type = type;
            this.senderName = senderName;
            this.status = status;
            this.text = text;
            this.timeSeconds = timeSeconds;
        }

        @Nullable
        @Override
        public FileInfo getMessageAttachment() {
            return attachment;
        }

        @Nullable
        @Override
        public String getAuthorId() {
            return authorId;
        }

        @Nullable
        @Override
        public String getMessageId() {
            return id;
        }

        @Nullable
        @Override
        public String getSenderName() {
            return senderName;
        }

        @Nullable
        @Override
        public String getMessageText() {
            return text;
        }

        @Nullable
        @Override
        public Type getMessageType() {
            return type;
        }

        @Override
        public long getMessageTimestamp() {
            return timeSeconds;
        }

        @NonNull
        @Override
        public State getState() {
            return status;
        }
    }

    public static class KeyboardImpl implements Keyboard {
        private final @NonNull Keyboard.State state;
        private final @Nullable List<List<KeyboardButtons>> keyboardButton;
        private final @Nullable KeyboardResponse keyboardResponse;

        public KeyboardImpl(
                @NonNull Keyboard.State state,
                @Nullable List<List<KeyboardButtons>> keyboardButton,
                @Nullable KeyboardResponse keyboardResponse) {
            this.state = state;
            this.keyboardButton = keyboardButton;
            this.keyboardResponse = keyboardResponse;
        }

        @Nullable
        @Override
        public List<List<KeyboardButtons>> getButtons() {
            return keyboardButton;
        }

        @NonNull
        @Override
        public Keyboard.State getState() {
            return state;
        }

        @Nullable
        @Override
        public KeyboardResponse getKeyboardResponse() {
            return keyboardResponse;
        }
    }

    public static class KeyboardButtonsImpl implements KeyboardButtons {
        private final @NonNull String id;
        private final @NonNull String text;
        private final @Nullable Configuration configuration;

        public KeyboardButtonsImpl(@NonNull String id, @NonNull String text, @Nullable Configuration configuration) {
            this.id = id;
            this.text = text;
            this.configuration = configuration;
        }

        @NonNull
        @Override
        public String getId() {
            return id;
        }

        @NonNull
        @Override
        public String getText() {
            return text;
        }

        @Nullable
        @Override
        public Configuration getConfiguration() {
            return configuration;
        }
    }

    public static class ConfigurationImpl implements KeyboardButtons.Configuration {
        private final @NonNull Type type;
        private final @NonNull State state;
        private final @NonNull String data;

        public ConfigurationImpl(@NonNull Type type, @NonNull State state, @NonNull String data) {
            this.type = type;
            this.state = state;
            this.data = data;
        }

        @NonNull
        @Override
        public Type getButtonType() {
            return type;
        }

        @NonNull
        @Override
        public String getData() {
            return data;
        }

        @NonNull
        @Override
        public State getState() {
            return state;
        }
    }

    public static class KeyboardResponseImpl implements KeyboardResponse {
        private final @NonNull String buttonId;
        private final @NonNull String messageId;

        public KeyboardResponseImpl(
                @NonNull String buttonId,
                @NonNull String messageId) {
            this.buttonId = buttonId;
            this.messageId = messageId;
        }

        @NonNull
        @Override
        public String getButtonId() {
            return buttonId;
        }

        @NonNull
        @Override
        public String getMessageId() {
            return messageId;
        }
    }

    public static class KeyboardRequestImpl implements Message.KeyboardRequest {
        private final @NonNull KeyboardButtons button;
        private final @NonNull String messageId;

        public KeyboardRequestImpl(
                @NonNull KeyboardButtons button,
                @NonNull String messageId) {
            this.button = button;
            this.messageId = messageId;
        }

        @NonNull
        @Override
        public KeyboardButtons getButtons() {
            return button;
        }

        @NonNull
        @Override
        public String getMessageId() {
            return messageId;
        }
    }

    public static class StickerImpl implements Message.Sticker {
        private int stickerId;

        public StickerImpl(int stickerId) {
            this.stickerId = stickerId;
        }

        @Override
        public int getStickerId() {
            return stickerId;
        }
    }
}
