package ru.webim.android.sdk.impl;

import static java.util.Collections.unmodifiableList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import ru.webim.android.sdk.Message;
import ru.webim.android.sdk.MessageListener;
import ru.webim.android.sdk.MessageTracker;
import ru.webim.android.sdk.Webim;
import ru.webim.android.sdk.WebimLogEntity;
import ru.webim.android.sdk.impl.backend.WebimInternalLog;
import ru.webim.android.sdk.impl.items.ChatItem;

public class MessageHolderImpl implements MessageHolder {
    private final AccessChecker accessChecker;
    private final RemoteHistoryProvider historyProvider;
    private final HistoryStorage historyStorage;
    private final List<MessageImpl> currentMessages = new ArrayList<>();
    private final List<MessageSending> sendingMessages = new ArrayList<>();
    private boolean isReachedEndOfLocalHistory = false;
    private boolean isReachedEndOfRemoteHistory;
    private boolean firstChatReceived;
    private int previousChatLastMessageIndex = 0;
    @Nullable
    private MessageTrackerImpl messageTracker;

    public MessageHolderImpl(AccessChecker accessChecker,
                             RemoteHistoryProvider historyProvider,
                             HistoryStorage historyStorage,
                             boolean isReachedEndOfRemoteHistory) {
        this.accessChecker = accessChecker;
        this.historyProvider = historyProvider;
        this.historyStorage = historyStorage;
        this.isReachedEndOfRemoteHistory = isReachedEndOfRemoteHistory;
    }

    private void checkAccess() {
        accessChecker.checkAccess();
    }

    @Override
    public void receiveHistoryUpdate(List<? extends MessageImpl> messages,
                                     Set<String> deleted,
                                     final Runnable callback) {
        historyStorage.receiveHistoryUpdate(
            messages,
            deleted,
            new HistoryStorage.UpdateHistoryCallback() {
                @Override
                public void onHistoryAdded(@Nullable String beforeServerSideId, @NonNull MessageImpl historyMessage) {
                    if (messageTracker != null) {
                        messageTracker.addNewOrMergeMessage(historyMessage, true);
                    }
                }

                @Override
                public void onHistoryChanged(@NonNull MessageImpl historyMessage) {
                    if (messageTracker != null) {
                        messageTracker.addNewOrMergeMessage(historyMessage, true);
                    }
                }

                @Override
                public void onHistoryDeleted(String serverSideId) {
                    onMessageDeleted(serverSideId);
                }

                @Override
                public void endOfBatch() {
                    callback.run();
                }
            });
    }

    public void setReachedEndOfRemoteHistory(boolean reachedEndOfRemoteHistory) {
        isReachedEndOfRemoteHistory = reachedEndOfRemoteHistory;
        historyStorage.setReachedEndOfRemoteHistory(reachedEndOfRemoteHistory);
    }

    @Override
    public MessageTracker newMessageTracker(@NonNull MessageListener messageListener) {
        if (messageTracker != null) {
            messageTracker.destroy();
        }

        return messageTracker = new MessageTrackerImpl(messageListener);
    }

    private void getLatestMessages(int limit, @NonNull MessageTracker.GetMessagesCallback callback) {
        if (!currentMessages.isEmpty()) {
            respondMessages(callback, currentMessages, limit);
        } else {
            long timestamp = TimeUnit.MILLISECONDS.toMicros(new Date().getTime());
            getMessagesFromHistory(timestamp, limit, callback);
        }
    }

    private void getMessages(@NonNull MessageImpl before,
                             int limit,
                             @NonNull final MessageTracker.GetMessagesCallback callback) {
        if (!currentMessages.isEmpty()) {
            MessageImpl first = currentMessages.get(0);
            if (before.equals(first)) {
                getMessagesFromHistory(first.getTimeMicros(), limit, callback);
            } else {
                getMessagesFromCurrent(before, limit, callback);
            }
        } else {
            getMessagesFromHistory(before.getTimeMicros(), limit, callback);
        }
    }

    private void getMessagesFromCurrent(@NonNull MessageImpl before,
                                        int limit,
                                        @NonNull final MessageTracker.GetMessagesCallback
                                                    callback) {
        int ind = currentMessages.indexOf(before);
        if (ind <= 0) {
            String sessionId = before.sessionId;
            String searchMessageId = before.getClientSideId().toString();
            String borderChatMessagesId =
                    currentMessages.get(0).getClientSideId().toString() +
                    " / " +
                    currentMessages.get(currentMessages.size() - 1).getClientSideId().toString();
            throw new IllegalStateException(
                    "Impossible: " +
                    sessionId + " / " +
                    searchMessageId + " / " +
                    borderChatMessagesId);
        }
        respondMessages(callback, currentMessages, ind, limit);
    }

    private void getMessagesFromHistory(long beforeTimestamp,
                                        final int limit,
                                        @NonNull final MessageTracker.GetMessagesCallback callback) {
        if (!isReachedEndOfLocalHistory) {
            historyStorage.getBefore(beforeTimestamp, limit, new MessageTracker.GetMessagesCallback() {
                @Override
                public void receive(@NonNull final List<? extends Message> messages) {
                    if (messages.isEmpty()) {
                        isReachedEndOfLocalHistory = true;
                        getMessagesFromHistory(beforeTimestamp, limit, callback);
                    } else if (messages.size() < limit && !isReachedEndOfRemoteHistory) {
                        requestMessagesFromRemoteHistory(messages);
                    } else {
                        callback.receive(messages);
                    }
                }

                private void requestMessagesFromRemoteHistory(@NonNull List<? extends Message> messages) {
                    final List<Message> result = Collections.synchronizedList(new ArrayList<>(messages));
                    requestHistoryBefore(
                        ((MessageImpl) messages.get(0)),
                        limit - messages.size(),
                        remoteMessages -> {
                            result.addAll(remoteMessages);
                            callback.receive(unmodifiableList(result));
                        }
                    );
                }
            });
        } else if (isReachedEndOfRemoteHistory) {
            callback.receive(Collections.emptyList());
        } else {
            requestHistoryBefore(beforeTimestamp, limit, callback);
        }
    }

    private void requestHistoryBefore(@NonNull MessageImpl before,
                                      final int limit,
                                      @NonNull final MessageTracker.GetMessagesCallback callback) {
        requestHistoryBefore(before.getTimeMicros(), limit, callback);
    }

    private void requestHistoryBefore(long timestamp,
                                      final int limit,
                                      @NonNull final MessageTracker.GetMessagesCallback callback) {
        historyProvider.requestHistoryBefore(
            timestamp,
            (messages, hasMore) -> {
                if (messages.isEmpty()) {
                    isReachedEndOfRemoteHistory = true;
                } else {
                    isReachedEndOfRemoteHistory = false;
                    historyStorage.receiveHistoryBefore(messages, hasMore);
                }
                respondMessages(callback, messages, limit);
            }
        );
    }

    private static void respondMessages(MessageTracker.GetMessagesCallback callback,
                                        List<? extends Message> messages,
                                        int limit) {
        callback.receive(messages.size() == 0
                ? Collections.emptyList()
                : unmodifiableList(
                messages.size() <= limit
                        ? messages
                        : messages.subList(messages.size() - limit, messages.size())));
    }

    private static void respondMessages(MessageTracker.GetMessagesCallback callback,
                                        List<? extends Message> messages,
                                        int offset,
                                        int limit) {
        callback.receive(unmodifiableList(messages.subList(Math.max(0, offset - limit), offset)));
    }

    public void onFirstFullUpdateReceived() {
        if (messageTracker != null && messageTracker.messagesSyncedListener != null) {
            messageTracker.messagesSyncedListener.messagesSynced();
        }
    }

    @Override
    public void onChatReceive(@Nullable ChatItem oldChat,
                              @Nullable ChatItem newChat,
                              List<? extends MessageImpl> newMessages) {
        if (oldChat == null && (newChat == null || newMessages.isEmpty())) {
            // if both chats're null or old chat is null and we got new chat with empty messages
            if (messageTracker != null && messageTracker.cachedCallback != null) {
                messageTracker.tryLoadOlderMessagesFromClosedChat();
            }
        } else if (!InternalUtils.equals(oldChat, newChat)) {
            // chats're not equals
            if (newChat == null) {
                historifyCurrentChat();
            }
            receiveNewMessages(newMessages);
        } else if (oldChat != null && newChat != null) {
            // both chats're equals and not null
            mergeCurrentChatWith(newMessages);
        }
        firstChatReceived = true;
    }

    private void historifyCurrentChat() {
        for (MessageImpl chatMessage : currentMessages) {
            if (messageTracker != null && (chatMessage.canBeEdited() || chatMessage.canBeReplied())) {
                chatMessage.setCanBeEdited(false);
                messageTracker.onCurrentMessageChanged(chatMessage, chatMessage);
            }
        }

        previousChatLastMessageIndex = currentMessages.size();
    }

    private void mergeCurrentChatWith(List<? extends MessageImpl> newChatMessages) {
        int oldMessageIndex = this.previousChatLastMessageIndex;
        List<MessageImpl> messagesToDelete = new ArrayList<>(currentMessages.subList(oldMessageIndex, currentMessages.size()));

        for (MessageImpl newMessage : newChatMessages) {
            boolean merged = false;
            while (oldMessageIndex < currentMessages.size()) {
                MessageImpl currentMessage = currentMessages.get(oldMessageIndex);
                if (currentMessage.equals(newMessage)) {
                    // update currentMessage
                    if (!currentMessage.isContentEquals(newMessage)) {
                        currentMessages.set(oldMessageIndex, newMessage);
                        if (messageTracker != null) {
                            messageTracker.onCurrentMessageChanged(currentMessage, newMessage);
                        }
                    }
                    merged = true;
                    messagesToDelete.remove(currentMessage);
                    oldMessageIndex++;
                    break;
                } else if (currentMessage.getTimeMicros() >= newMessage.getTimeMicros()) {
                    break;
                }
                oldMessageIndex++;
            }

            if (!merged) {
                int nextMessageIndex = oldMessageIndex + 1;
                MessageImpl beforeMessage = getNextCurrentMessage(nextMessageIndex);
                currentMessages.add(oldMessageIndex, newMessage);
                if (messageTracker != null) {
                    messageTracker.onCurrentMessageAdded(beforeMessage, newMessage);
                }
                oldMessageIndex++;
            }
        }

        if (!messagesToDelete.isEmpty()) {
            for (int i = 0; i < messagesToDelete.size(); i++) {
                MessageImpl needDeleteMessage = messagesToDelete.get(i);
                currentMessages.remove(needDeleteMessage);
                if (messageTracker != null) {
                    messageTracker.onCurrentMessageDeleted(i, needDeleteMessage);
                }
            }
        }
    }

    @Nullable
    private MessageImpl getNextCurrentMessage(int nextMessageIndex) {
        return currentMessages.size() > nextMessageIndex ? currentMessages.get(nextMessageIndex) : null;
    }

    private void receiveNewMessages(@NonNull List<? extends MessageImpl> newMessages) {
        if (!newMessages.isEmpty()) {
            if (messageTracker != null) {
                messageTracker.onNewMessages(newMessages);
            } else {
                currentMessages.addAll(newMessages);
            }
        }
    }

    @Override
    public void onMessageAdded(@NonNull MessageImpl newMessage) {
        if (messageTracker != null) {
            MessageImpl headMessage = messageTracker.headMessage;
            if (headMessage == null || headMessage.compareTo(newMessage) > 0) {
                messageTracker.headMessage = newMessage;
            }
            messageTracker.addNewOrMergeMessage(newMessage, false);
        } else {
            currentMessages.add(newMessage);
        }
    }

    @Override
    public void onMessageChanged(@NonNull MessageImpl newMessage) {
        if (messageTracker != null) {
            messageTracker.addNewOrMergeMessage(newMessage, false);
        }
    }

    @Override
    public void onMessageDeleted(@NonNull String messageId) {
        for (int i = previousChatLastMessageIndex; i < currentMessages.size(); i++) {
            MessageImpl oldMessage = currentMessages.get(i);
            if (oldMessage.getServerSideId().equals(messageId)) {
                currentMessages.remove(i);
                if (messageTracker != null) {
                    messageTracker.onCurrentMessageDeleted(i, oldMessage);
                }
                break;
            }
        }
    }

    @Override
    public void onSendingMessage(@NonNull MessageSending message) {
        sendingMessages.add(message);
        if (messageTracker != null) {
            messageTracker.messageListener.messageAdded(null, message);
        }
    }

    @Override
    @Nullable
    public String onChangingMessage(@NonNull Message.Id id, @Nullable String text) {
        if (messageTracker != null) {
            MessageImpl message = null;
            for (MessageImpl curr : currentMessages) {
                if (curr.clientSideId.equals(id)) {
                    message = curr;
                    break;
                }
            }
            if (message == null) {
                return null;
            }

            MessageImpl newMsg = new MessageImpl(
                    message.serverUrl,
                    message.clientSideId,
                    message.sessionId,
                    message.operatorId,
                    message.avatarUrl,
                    message.senderName,
                    message.type,
                    text == null ? message.text : text,
                    message.timeMicros,
                    message.getServerSideId(),
                    message.getData(),
                    message.savedInHistory,
                    message.getAttachment(),
                    message.isReadByOperator(),
                    message.canBeEdited(),
                    message.canBeReplied(),
                    message.isEdited(),
                    message.getQuote(),
                    message.getKeyboard(),
                    message.getKeyboardRequest(),
                    message.getSticker(),
                    message.getReaction(),
                    message.canVisitorReact(),
                    message.canVisitorChangeReaction());
            newMsg.sendStatus = Message.SendStatus.SENDING;
            messageTracker.messageListener.messageChanged(message, newMsg);
            return message.text;
        }
        return null;
    }

    @Override
    public void onMessageSendingCancelled(@NonNull Message.Id id) {
        for (Iterator<MessageSending> iterator = sendingMessages.iterator(); iterator.hasNext(); ) {
            MessageSending msg = iterator.next();
            if (msg.getClientSideId().equals(id)) {
                iterator.remove();
                if (messageTracker != null) {
                    messageTracker.messageListener.messageRemoved(msg);
                }
                break;
            }
        }
    }

    @Override
    public void onMessageChangingCancelled(@NonNull Message.Id id, @NonNull String text) {
        if (messageTracker != null) {
            MessageImpl message = null;
            for (MessageImpl curr : currentMessages) {
                if (curr.clientSideId.equals(id)) {
                    message = curr;
                    break;
                }
            }
            if (message == null) {
                return;
            }

            MessageImpl newMsg = new MessageImpl(
                    message.serverUrl,
                    message.clientSideId,
                    message.sessionId,
                    message.operatorId,
                    message.avatarUrl,
                    message.senderName,
                    message.type,
                    text,
                    message.timeMicros,
                    message.getServerSideId(),
                    message.getData(),
                    message.savedInHistory,
                    message.getAttachment(),
                    message.isReadByOperator(),
                    message.canBeEdited(),
                    message.canBeReplied(),
                    message.isEdited(),
                    message.getQuote(),
                    message.getKeyboard(),
                    message.getKeyboardRequest(),
                    message.getSticker(),
                    message.getReaction(),
                    message.canVisitorReact(),
                    message.canVisitorChangeReaction());
            newMsg.sendStatus = Message.SendStatus.SENT;
            messageTracker.messageListener.messageChanged(message, newMsg);
        }
    }

    @Override
    public void updateReadBeforeTimestamp(Long timestamp) {
        historyStorage.getReadBeforeTimestampListener().onTimestampChanged(timestamp);
    }

    @Override
    public boolean historyMessagesEmpty() {
        return messageTracker == null || currentMessages.isEmpty();
    }

    @Override
    public void clearHistory() {
        historyStorage.clearHistory();

        ListIterator<MessageImpl> listIterator = currentMessages.listIterator();
        while (listIterator.hasNext()) {
            int nextIndex = listIterator.nextIndex();
            MessageImpl message = listIterator.next();
            listIterator.remove();
            if (messageTracker != null) {
                messageTracker.onCurrentMessageDeleted(nextIndex, message);
            }
        }
    }

    private class MessageTrackerImpl implements MessageTracker {
        private final MessageListener messageListener;
        private MessageImpl headMessage;
        private boolean isMessagesLoading;
        private boolean isAllMessageSourcesEnded;
        private boolean isDestroyed;
        private boolean isLoadingFromClosedChat;

        private MessagesSyncedListener messagesSyncedListener;
        private GetMessagesCallback cachedCallback;
        private int cachedLimit;

        private MessageTrackerImpl(MessageListener messageListener) {
            this.messageListener = messageListener;
        }

        @Override
        public void getNextMessages(final int limitOfMessages, @NonNull final GetMessagesCallback callback) {
            checkAccess();

            if (isDestroyed) {
                throw new IllegalStateException("WebimMessageTracker is destroyed");
            }
            if (isMessagesLoading) {
                throw new IllegalStateException("Messages is loading now; can't load messages in parallel");
            }
            if (limitOfMessages <= 0) {
                throw new IllegalArgumentException("limit must be greater than zero. Given " + limitOfMessages);
            }
            isMessagesLoading = true;
            if (firstChatReceived) {
                uncheckedGetNextMessages(limitOfMessages, callback);
            } else {
                cachedCallback = callback;
                cachedLimit = limitOfMessages;

                historyStorage.getLatest(limitOfMessages, messages -> {
                    if (cachedCallback != null && !messages.isEmpty() && !isLoadingFromClosedChat) {
                        final GetMessagesCallback callback1 = cachedCallback;
                        cachedCallback = null;
                        cachedLimit = 0;
                        new GetMessagesCallbackWrapper(limitOfMessages, callback1).receive(messages);
                    }
                });
            }
        }

        @Override
        public void getLastMessages(final int limitOfMessages, GetMessagesCallback callback) {
            checkAccess();

            if (isDestroyed) {
                throw new IllegalStateException("WebimMessageTracker is destroyed");
            }
            if (isMessagesLoading) {
                throw new IllegalStateException("Messages is loading now; " +
                        "can't load messages in parallel");
            }
            if (limitOfMessages <= 0) {
                throw new IllegalArgumentException("limit must be greater than zero. " +
                        "Given " + limitOfMessages);
            }

            isMessagesLoading = true;
            isReachedEndOfRemoteHistory = false;
            isReachedEndOfLocalHistory = false;
            isAllMessageSourcesEnded = false;
            headMessage = null;

            if (firstChatReceived) {
                getLatestMessages(limitOfMessages, new GetMessagesCallbackWrapper(limitOfMessages, callback));
            } else {
                cachedCallback = callback;
                cachedLimit = limitOfMessages;

                historyStorage.getLatest(limitOfMessages, messages -> {
                    if (cachedCallback != null && !messages.isEmpty() && !isLoadingFromClosedChat) {
                        final GetMessagesCallback callback1 = cachedCallback;
                        cachedCallback = null;
                        cachedLimit = 0;
                        new GetMessagesCallbackWrapper(limitOfMessages, callback1).receive(messages);
                    }
                });
            }
        }

        @Override
        public void getAllMessages(final GetMessagesCallback callback) {
            checkAccess();

            if (isDestroyed) {
                throw new IllegalStateException("WebimMessageTracker is destroyed");
            }
            if (isMessagesLoading) {
                throw new IllegalStateException("Messages is loading now; " +
                    "can't load messages in parallel");
            }
            isMessagesLoading = true;

            historyStorage.getFull(messages -> new GetMessagesCallbackWrapper(0, callback).receive(messages));
        }

        @Override
        public void setMessagesSyncedListener(@Nullable MessagesSyncedListener messagesSyncedListener) {
            this.messagesSyncedListener = messagesSyncedListener;
        }

        private void uncheckedGetNextMessages(int limit,
                                              @NonNull final GetMessagesCallback callback) {
            GetMessagesCallback callback2 = new GetMessagesCallbackWrapper(limit, callback);
            if (headMessage == null) {
                getLatestMessages(limit, callback2);
            } else {
                getMessages(headMessage, limit, callback2);
            }
        }

        @Override
        public void resetTo(@NonNull Message message) {
            checkAccess();

            if (isDestroyed) {
                throw new IllegalStateException("WebimMessageTracker is destroyed");
            }
            if (isMessagesLoading) {
                throw new IllegalStateException("Messages is loading now; can't reset in parallel");
            }

            MessageImpl unwrappedMessage = (MessageImpl) message;
            if (!unwrappedMessage.equals(headMessage)) {
                isReachedEndOfLocalHistory = false;
            }
            List<MessageImpl> deleted = new ArrayList<>();
            for (int i = 0; i < currentMessages.size(); i++) {
                MessageImpl iteratedMessage = currentMessages.get(i);
                if (iteratedMessage.getTimeMicros() < unwrappedMessage.getTimeMicros()) {
                    deleted.add(iteratedMessage);
                    if (previousChatLastMessageIndex >= i) {
                        previousChatLastMessageIndex--;
                    }
                }
            }

            for (MessageImpl messageToDelete : deleted) {
                int index = currentMessages.indexOf(messageToDelete);
                onCurrentMessageDeleted(index + 1, messageToDelete);
            }
            headMessage = unwrappedMessage;
        }

        @Override
        public void destroy() {
            checkAccess();

            if (!isDestroyed) {
                isDestroyed = true;

                sendingMessages.clear();

                if (messageTracker == this) {
                    messageTracker = null;
                }
            }
        }

        @Override
        public void loadAllHistorySince(@NonNull final Message sinceMessage, @NonNull final GetMessagesCallback messagesCallback) {
            final List<MessageImpl> searchedMessages = new LinkedList<>();
            historyStorage.getFull(new GetMessagesCallback() {
                @Override
                public void receive(@NonNull List<? extends Message> localStorageMessages) {
                    int sinceMessagePosition = -1;
                    for (int i = 0; i < localStorageMessages.size(); i++) {
                        Message currentLocalMessage = localStorageMessages.get(i);
                        if (currentLocalMessage.getClientSideId().equals(sinceMessage.getClientSideId())) {
                            sinceMessagePosition = i;
                            break;
                        }
                    }
                    if (sinceMessagePosition != -1) {
                        for (int i = sinceMessagePosition; i < localStorageMessages.size(); i++) {
                            searchedMessages.add((MessageImpl) localStorageMessages.get(i));
                        }
                        headMessage = searchedMessages.get(0);
                        messagesCallback.receive(searchedMessages);
                        return;
                    }
                    long beforeMillis = localStorageMessages.isEmpty()
                            ? new Date().getTime()
                            : localStorageMessages.get(0).getTime();
                    loadHistoryBefore(TimeUnit.MILLISECONDS.toMicros(beforeMillis), new HistoryBeforeCallback() {

                        @Override
                        public void onSuccess(List<? extends MessageImpl> messages, boolean hasMore) {
                            if (messages.isEmpty()) {
                                headMessage = null;
                                messagesCallback.receive(Collections.<Message>emptyList());
                                return;
                            }

                            MessageImpl lastMessage = messages.get(0);
                            if (((MessageImpl) sinceMessage).getTimeMicros() < lastMessage.getTimeMicros()) {
                                searchedMessages.addAll(0, messages);
                                loadHistoryBefore(lastMessage.getTimeMicros(), this);
                            } else {
                                for (int i = messages.size() - 1; i >= 0; i--) {
                                    MessageImpl historyMessage = messages.get(i);
                                    searchedMessages.add(0, historyMessage);
                                    if (historyMessage.getClientSideId().equals(sinceMessage.getClientSideId())) {
                                        headMessage = searchedMessages.get(0);
                                        messagesCallback.receive(searchedMessages);
                                        return;
                                    }
                                }

                                // if messages were not found
                                headMessage = null;
                                messagesCallback.receive(Collections.emptyList());
                            }
                        }
                    });
                }
            });
        }

        private void loadHistoryBefore(long beforeMicros, HistoryBeforeCallback historyBeforeCallback) {
            historyProvider.requestHistoryBefore(beforeMicros, historyBeforeCallback);
        }

        void onNewMessages(final List<? extends MessageImpl> newMessages) {
            if (cachedCallback == null) {
                if (headMessage == null && firstChatReceived) {
                    headMessage = newMessages.get(0);
                }
                for (MessageImpl message : newMessages) {
                    addNewOrMergeMessage(message, false);
                }
            } else {
                currentMessages.addAll(newMessages);
                GetMessagesCallback callback = cachedCallback;
                int limit = cachedLimit;
                cachedCallback = null;
                cachedLimit = 0;
                uncheckedGetNextMessages(limit, callback);
            }
        }

        private void addNewOrMergeMessage(final MessageImpl newMessage, boolean isFromHistory) {
            int currentIndex;
            boolean addToEnd = true;
            for (currentIndex = 0; currentIndex < currentMessages.size(); currentIndex++) {
                MessageImpl message = currentMessages.get(currentIndex);
                if (newMessage.equals(message)) {
                    // New message will be merged
                    if (isFromHistory && !message.isContentEqualsForHistoryMessage(newMessage)) {
                        MessageImpl messageTransformed = message.mergeWithHistoryMessage(newMessage);
                        currentMessages.set(currentIndex, messageTransformed);
                        onCurrentMessageChanged(message, messageTransformed);
                    } else if (!isFromHistory && !message.isContentEquals(newMessage)) {
                        currentMessages.set(currentIndex, newMessage);
                        if (message.isSavedInHistory()) {
                            newMessage.setSavedInHistory(true);
                        }
                        newMessage.setCanBeReplied(true);
                        onCurrentMessageChanged(message, newMessage);
                    }
                    return;
                }
                if (message.compareTo(newMessage) > 0) {
                    addToEnd = false;
                    break;
                }
            }

            // New message will be added
            MessageSending messageToSend = findMessageSendingMirror(newMessage);
            if (messageToSend != null) {
                sendingMessages.remove(messageToSend);
                currentMessages.add(newMessage);
                onCurrentMessageChanged(messageToSend, newMessage);
            } else {
                if (previousChatLastMessageIndex > currentIndex) {
                    previousChatLastMessageIndex++;
                }
                if (addToEnd) {
                    currentMessages.add(newMessage);
                    onCurrentMessageAdded(findEarliestSendingMessage(), newMessage);
                } else {
                    MessageImpl beforeMessage = currentMessages.get(currentIndex);
                    currentMessages.add(currentIndex, newMessage);
                    onCurrentMessageAdded(beforeMessage, newMessage);
                }
            }
        }

        @Nullable
        private MessageSending findEarliestSendingMessage() {
            MessageSending earliest = null;
            for (MessageSending sendingMessage : sendingMessages) {
                if (sendingMessage.compareTo(earliest) > 0) {
                    earliest = sendingMessage;
                }
            }
            return earliest;
        }

        @Nullable
        private MessageSending findMessageSendingMirror(MessageImpl newMessage) {
            for (MessageSending sendingMessage : sendingMessages) {
                if (sendingMessage.equals(newMessage)) {
                    return sendingMessage;
                }
            }
            return null;
        }

        void onCurrentMessageAdded(MessageImpl before, MessageImpl msg) {
            if (headMessage != null && msg.compareTo(headMessage) >= 0) {
                String beforeText = "null";
                if (before != null) {
                    beforeText = before.getText();
                }
                WebimInternalLog.getInstance().log("Added \"" + msg.getText() + "\" after: \"" + beforeText + "\"",
                        Webim.SessionBuilder.WebimLogVerbosityLevel.VERBOSE,
                        WebimLogEntity.MESSAGES
                );
                messageListener.messageAdded(before, msg);
            }
        }

        void onCurrentMessageChanged(MessageImpl from, MessageImpl to) {
            if (headMessage != null && to.compareTo(headMessage) >= 0) {
                WebimInternalLog.getInstance().log("Changed \"" + from.getText() + "\" to: \"" + to.getText() + "\"",
                        Webim.SessionBuilder.WebimLogVerbosityLevel.VERBOSE,
                        WebimLogEntity.MESSAGES
                );
                messageListener.messageChanged(from, to);
            }
        }

        void onCurrentMessageDeleted(int nextMessageIndex, MessageImpl msg) {
            if (headMessage != null && msg.compareTo(headMessage) >= 0) {
                messageListener.messageRemoved(msg);

                if (headMessage == msg) {
                    headMessage = getNextCurrentMessage(nextMessageIndex);
                }
            }
        }

        public void tryLoadOlderMessagesFromClosedChat() {
            isLoadingFromClosedChat = true;
            long timestamp = TimeUnit.MILLISECONDS.toMicros(new Date().getTime());
            getMessagesFromHistory(
                timestamp,
                cachedLimit,
                messages -> {
                    if (cachedCallback != null) {
                        GetMessagesCallback callback = cachedCallback;
                        int limit = cachedLimit;
                        cachedCallback = null;
                        cachedLimit = 0;
                        new MessageTrackerImpl.GetMessagesCallbackWrapper(limit, callback).receive(messages);
                        isLoadingFromClosedChat = false;
                    }
                }
            );
        }

        private class GetMessagesCallbackWrapper implements GetMessagesCallback {
            private int limit;
            private final GetMessagesCallback wrapped;

            private GetMessagesCallbackWrapper(int limit, GetMessagesCallback wrapped) {
                this.limit = limit;
                this.wrapped = wrapped;
            }

            @Override
            public void receive(@NonNull List<? extends Message> messages) {
                if (isDestroyed) {
                    return;
                }
                List<? extends Message> result;
                if (!messages.isEmpty()) {
                    List<MessageImpl> filtered = new ArrayList<>(messages.size());
                    merge(new ArrayList<>((List<MessageImpl>) messages), filtered);

                    if (filtered.isEmpty()) {
                        getMessages((MessageImpl) messages.get(0), limit, this);
                        return;
                    } else {
                        MessageImpl first = filtered.get(0);
                        if (headMessage == null || first.compareTo(headMessage) < 0) {
                            headMessage = first;
                        }
                    }
                    result = filtered;
                } else {
                    result = messages;
                    isAllMessageSourcesEnded = true;
                }
                isMessagesLoading = false;
                wrapped.receive(unmodifiableList(result));
            }

            private void merge(@NonNull List<MessageImpl> newMessages, @NonNull List<MessageImpl> filtered) {
                int oldMessageIndex = 0;
                for (int i = 0; i < newMessages.size(); i++) {
                    MessageImpl newMessage = newMessages.get(i);
                    boolean merged = false;
                    while (oldMessageIndex < currentMessages.size()) {
                        MessageImpl currentMessage = currentMessages.get(oldMessageIndex);
                        if (currentMessage.equals(newMessage)) {
                            if (headMessage == null || newMessage.compareTo(headMessage) < 0) {
                                filtered.add(newMessage);
                            }
                            merged = true;
                            oldMessageIndex++;
                            break;
                        } else if (currentMessage.compareTo(newMessage) >= 0) {
                            break;
                        }
                        oldMessageIndex++;
                    }

                    if (!merged) {
                        currentMessages.add(oldMessageIndex, newMessage);
                        filtered.add(newMessage);
                        if (previousChatLastMessageIndex > oldMessageIndex) {
                            previousChatLastMessageIndex++;
                        }
                        oldMessageIndex++;
                    }
                }
            }
        }
    }
}
