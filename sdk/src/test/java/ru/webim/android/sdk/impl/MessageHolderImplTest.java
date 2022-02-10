package ru.webim.android.sdk.impl;

import ru.webim.android.sdk.Message;
import ru.webim.android.sdk.MessageListener;
import ru.webim.android.sdk.MessageTracker;
import ru.webim.android.sdk.impl.items.ChatItem;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MessageHolderImplTest {

    private Runnable EMPTY_RUNNABLE = () -> {};

    private List<MessageImpl> EMPTY_MESSAGES_LIST = Collections.emptyList();

    private <T> Set<T> setOf(T ... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    private <T> List<T> listOf(T ... elements) {
        return Arrays.asList(elements);
    }

    private ArgumentCaptor<List<Message>> newListCaptor() {
        return ArgumentCaptor.forClass(List.class);
    }

    private ArgumentCaptor<Message> newMessageCaptor() {
        return ArgumentCaptor.forClass(Message.class);
    }

    static class History implements RemoteHistoryProvider {
        List<MessageImpl> history;

        History(List<MessageImpl> history) {
            this.history = history;
        }

        @Override
        public void requestHistoryBefore(long beforeMessageTs, HistoryBeforeCallback callback) {
            int before = -1;
            for (int i = history.size() - 1; i >= 0; i--) {
                MessageImpl historyMessage = history.get(i);
                if (historyMessage.getTimeMicros() == beforeMessageTs) {
                    before = i;
                    break;
                } else if (historyMessage.getTimeMicros() < beforeMessageTs) {
                    before = i + 1;
                    break;
                }
            }
            int after = Math.max(0, before - 100);
            callback.onSuccess(
                before <= 0
                    ? Collections.emptyList()
                    : history.subList(after, before),
                after != 0
            );
        }
    }

    private List<MessageImpl> generateHistoryInRange(int first, int last) {
        List<MessageImpl> history = new ArrayList<>();
        for (int i = first; i < last; i++) {
            history.add(new MessageImpl(
                "",
                StringId.forMessage(String.valueOf(i)),
                null,
                StringId.forOperator("op"),
                "",
                "",
                Message.Type.OPERATOR,
                "text",
                i * 1000,
                String.valueOf(i),
                null,
                true,
                null,
                false,
                false,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                false,
                false
            ));
        }
        return history;
    }

    private List<MessageImpl> generateCurrentChatInRange(int first, int last, Set<Integer> expect) {
        List<MessageImpl> currentChatMessages = new ArrayList<>();
        for (int i = first; i < last; i++) {
            if (expect.contains(i)) {
                continue;
            }
            currentChatMessages.add(new MessageImpl(
                "",
                StringId.forMessage(String.valueOf(i)),
                null,
                StringId.forOperator("op"),
                "",
                "",
                Message.Type.OPERATOR,
                "text",
                i * 1000,
                String.valueOf(i),
                null,
                false,
                null,
                false,
                true,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                false,
                false
            ));
        }
        return currentChatMessages;
    }

    private List<MessageImpl> generateHistoryFromCurrentChat(List<? extends MessageImpl> input) {
        List<MessageImpl> historyMessages = new ArrayList<>();
        for (MessageImpl currentChatMessage : input) {
            historyMessages.add(
                new MessageImpl(
                    "",
                    currentChatMessage.getClientSideId(),
                    currentChatMessage.getSessionId(),
                    currentChatMessage.getOperatorId(),
                    currentChatMessage.getAvatarUrlLastPart(),
                    currentChatMessage.getSenderName(),
                    currentChatMessage.getType(),
                    currentChatMessage.getText(),
                    currentChatMessage.timeMicros,
                    String.valueOf(currentChatMessage.timeMicros),
                    null,
                    true,
                    null,
                    false,
                    true,
                    false,
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    false
                )
            );
        }
        return historyMessages;
    }

    private MessageImpl newCurrentChatMessage(int index) {
        return new MessageImpl(
            "",
            StringId.forMessage(String.valueOf(index)),
            null,
            StringId.forOperator("op"),
            "",
            "",
            Message.Type.OPERATOR,
            "text",
            index * 1000,
            String.valueOf(index),
            null,
            false,
            null,
            false,
            true,
            false,
            false,
            null,
            null,
            null,
            null,
            null,
            false,
            false
        );
    }

    private MessageImpl newEditedCurrentChatMessage(MessageImpl original) {
        return new MessageImpl(
            "",
            original.getClientSideId(),
            original.getSessionId(),
            original.getOperatorId(),
            original.getAvatarUrlLastPart(),
            original.getSenderName(),
            original.getType(),
            original.getText() + "1",
            original.getTimeMicros(),
            original.getServerSideId(),
            null,
            false,
            null,
            false,
            true,
            false,
            false,
            null,
            null,
            null,
            null,
            null,
            false,
            false
        );
    }

    private MessageImpl newEditedHistoryMessage(MessageImpl original) {
        return new MessageImpl(
            "",
            original.getClientSideId(),
            original.getSessionId(),
            original.getOperatorId(),
            original.getAvatarUrlLastPart(),
            original.getSenderName(),
            original.getType(),
            original.getText() + "1",
            original.getTimeMicros(),
            original.getServerSideId(),
            null,
            true,
            null,
            false,
            true,
            false,
            false,
            null,
            null,
            null,
            null,
            null,
            false,
            false
        );
    }

    private MessageSending newSendingMessage(int index) {
        return new MessageSending(
            "",
            StringId.forMessage(String.valueOf(index)),
            "",
            Message.Type.VISITOR,
            String.valueOf(index),
            index * 1000,
            null,
            null,
            null
        );
    }

    private MessageHolderImpl newMessageHolderWithHistory(RemoteHistoryProvider history) {
        return new MessageHolderImpl(AccessChecker.EMPTY, history, new MemoryHistoryStorage(), false);
    }

    private MessageHolderImpl newMessageHolderWithHistory(RemoteHistoryProvider history, List<MessageImpl> localHistory) {
        return new MessageHolderImpl(AccessChecker.EMPTY, history, new MemoryHistoryStorage(localHistory), false);
    }

    private List<MessageImpl> concat(List<MessageImpl> list1, List<MessageImpl> list2) {
        List<MessageImpl> resultList = new ArrayList<>();
        resultList.addAll(list1);
        resultList.addAll(list2);
        return resultList;
    }

    private List<MessageImpl> cloneCurrentChatMessages(List<MessageImpl> currentMessages) {
        try {
            Method method = currentMessages.getClass().getDeclaredMethod("clone");
            method.setAccessible(true);
            return (List<MessageImpl>) method.invoke(currentMessages);
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private void setPrivateField(String privateFieldName, Object privateFieldValue, Object object) {
        try {
            Field field = object.getClass().getDeclaredField(privateFieldName);
            field.setAccessible(true);
            field.set(object, privateFieldValue);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void checkCapturedMessages(List<Message> captored, List<MessageImpl> messages) {
        if (captored.size() != messages.size()) {
            throw new IllegalStateException("count of captored messages is invalid");
        }
        for (int i = 0; i < messages.size(); i++) {
            assertEquals(messages.get(i), captored.get(i));
        }
    }

    @Test
    public void internalHistoryGeneration() {
        List<MessageImpl> history1 = generateHistoryInRange(0, 10);
        List<MessageImpl> history3 = generateHistoryInRange(20, 30);

        List<String> ids1 = history1.stream()
            .map(MessageImpl::getServerSideId)
            .collect(Collectors.toList());

        List<String> ids3 = history3.stream()
            .map(MessageImpl::getServerSideId)
            .collect(Collectors.toList());

        assertEquals(ids1, IntStream.rangeClosed(0, 9).boxed().map(String::valueOf).collect(Collectors.toList()));
        assertEquals(ids3, IntStream.rangeClosed(20, 29).boxed().map(String::valueOf).collect(Collectors.toList()));
    }

    @Test
    public void respondCurrentChatMessagesImmediately() {
        List<MessageImpl> currentChatMessages = generateCurrentChatInRange(0, 10, setOf());
        MessageHolder messageHolder = newMessageHolderWithHistory(new History(Collections.<MessageImpl>emptyList()));
        MessageListener messageListener = mock(MessageListener.class);
        MessageTracker messageTracker = messageHolder.newMessageTracker(messageListener);
        MessageTracker.GetMessagesCallback callback = mock(MessageTracker.GetMessagesCallback.class);

        messageHolder.onChatReceive(null, new ChatItem(), currentChatMessages, true);

        messageTracker.getNextMessages(10, callback);
        verify(callback, times(1)).receive(currentChatMessages);
    }

    @Test
    public void checkGetNextMessages() {
        List<MessageImpl> currentChatMessages = generateCurrentChatInRange(0, 10, setOf());
        List<MessageImpl> history = generateHistoryFromCurrentChat(currentChatMessages);
        MessageHolder messageHolder = newMessageHolderWithHistory(new History(listOf()));
        MessageListener messageListener = mock(MessageListener.class);
        MessageTracker messageTracker = messageHolder.newMessageTracker(messageListener);
        MessageTracker.GetMessagesCallback callback = mock(MessageTracker.GetMessagesCallback.class);

        // This method caches callback
        messageTracker.getNextMessages(10, callback);
        // Callback will be cached because local storage is empty.
        verify(callback, never()).receive(Mockito.any());

        reset(callback);
        messageHolder.onChatReceive(null, new ChatItem(), currentChatMessages, true);
        // Our cached callback will be fired.
        verify(callback, times(1)).receive(currentChatMessages);

        reset(messageListener);
        // History messages will be saved to local storage and merged with currentMessages
        messageHolder.receiveHistoryUpdate(history, setOf(), EMPTY_RUNNABLE);
        verify(messageListener, times(currentChatMessages.size())).messageChanged(Mockito.any(), Mockito.any());
    }

    @Test
    public void checkGetAllMessages() {
        List<MessageImpl> history1 = generateHistoryInRange(0, 10);
        List<MessageImpl> currentChatMessages = generateCurrentChatInRange(10, 20, setOf());
        List<MessageImpl> history2 = generateHistoryFromCurrentChat(currentChatMessages);
        MessageHolder messageHolder = newMessageHolderWithHistory(new History(listOf()));
        MessageListener messageListener = mock(MessageListener.class);
        MessageTracker messageTracker = messageHolder.newMessageTracker(messageListener);
        MessageTracker.GetMessagesCallback callback = mock(MessageTracker.GetMessagesCallback.class);

        // This method doesn't cache callback, it just gets all messages from local storage
        messageTracker.getAllMessages(callback);
        // Local storage is empty, so we get empty list
        verify(callback, times(1)).receive(EMPTY_MESSAGES_LIST);

        messageHolder.onChatReceive(null, new ChatItem(), currentChatMessages, true);
        // history1 will be saved to local storage
        messageHolder.receiveHistoryUpdate(history1, setOf(), EMPTY_RUNNABLE);
        // history2 will be saved to local storage
        messageHolder.receiveHistoryUpdate(history2, setOf(), EMPTY_RUNNABLE);

        reset(callback);
        messageTracker.getAllMessages(callback);
        verify(callback, times(1)).receive(concat(history1, history2));
    }

    @Test
    public void checkGetLastMessages() {
        List<MessageImpl> currentChatMessages = generateCurrentChatInRange(0, 10, setOf());
        List<MessageImpl> history1 = generateHistoryFromCurrentChat(currentChatMessages);
        List<MessageImpl> history2 = generateHistoryInRange(10, 20);
        MessageHolder messageHolder = newMessageHolderWithHistory(new History(Collections.<MessageImpl>emptyList()));
        MessageListener messageListener = mock(MessageListener.class);
        MessageTracker messageTracker = messageHolder.newMessageTracker(messageListener);
        MessageTracker.GetMessagesCallback callback = mock(MessageTracker.GetMessagesCallback.class);

        // This method caches callback
        messageTracker.getLastMessages(10, callback);
        // Local storage is empty, so callback is cached and will not be fired
        verify(callback, never()).receive(Mockito.any());

        reset(callback);
        messageHolder.onChatReceive(null, new ChatItem(), currentChatMessages, true);
        // Fire cached callback
        verify(callback, times(1)).receive(currentChatMessages);

        reset(messageListener);
        messageHolder.receiveHistoryUpdate(history1, setOf(), EMPTY_RUNNABLE);
        verify(messageListener, times(history1.size())).messageChanged(Mockito.any(), Mockito.any());
        messageHolder.receiveHistoryUpdate(history2, setOf(), EMPTY_RUNNABLE);
        verify(messageListener, times(history2.size())).messageAdded(Mockito.isNull(), Mockito.any());

        reset(callback);
        messageTracker.getLastMessages(10, callback);
        verify(callback, times(1)).receive(history2);
    }

    @Test
    public void checkResetTo() {
        List<MessageImpl> currentChatMessages = generateCurrentChatInRange(0, 10, setOf());
        MessageHolder messageHolder = newMessageHolderWithHistory(new History(Collections.<MessageImpl>emptyList()));
        MessageListener messageListener = mock(MessageListener.class);
        MessageTracker messageTracker = messageHolder.newMessageTracker(messageListener);
        MessageTracker.GetMessagesCallback callback = mock(MessageTracker.GetMessagesCallback.class);

        messageHolder.onChatReceive(null, new ChatItem(), currentChatMessages, true);

        messageTracker.getNextMessages(8, callback);
        verify(callback, times(1)).receive(currentChatMessages.subList(2, 10));

        // Now headMessage is set to message with index '2', let's reset currentMessages to '5'
        reset(messageListener);
        messageTracker.resetTo(currentChatMessages.get(5));
        // Check that for messages from '2' to '5' was called callback
        verify(messageListener, times(3)).messageRemoved(any());

        reset(callback);
        messageTracker.getNextMessages(5, callback);
        // Here we must get messages from '5' to '0'
        verify(callback, times(1)).receive(currentChatMessages.subList(0, 5));

        reset(messageListener);
        messageTracker.resetTo(currentChatMessages.get(9));
        verify(messageListener, times(9)).messageRemoved(any());

        reset(callback);
        messageTracker.getNextMessages(10, callback);
        verify(callback, times(1)).receive(currentChatMessages.subList(0, 9));
    }

    @Test
    public void checkMergeCurrentChatWith() {
        List<MessageImpl> currentChatMessages1 = generateCurrentChatInRange(0,10, setOf(1, 2, 3));
        List<MessageImpl> currentChatMessages2 = generateCurrentChatInRange(0,10, setOf(8));
        MessageHolder messageHolder = newMessageHolderWithHistory(new History(Collections.<MessageImpl>emptyList()));
        MessageListener messageListener = mock(MessageListener.class);
        MessageTracker messageTracker = messageHolder.newMessageTracker(messageListener);
        MessageTracker.GetMessagesCallback callback = mock(MessageTracker.GetMessagesCallback.class);

        ChatItem chatItem = new ChatItem();
        // set these private fields, to make chat items equals
        setPrivateField("id", "1id", chatItem);
        setPrivateField("clientSideId", "1csi", chatItem);

        assertEquals(chatItem, chatItem);

        messageHolder.onChatReceive(null, chatItem, currentChatMessages1, true);
        messageTracker.getNextMessages(10, callback);
        verify(callback, times(1)).receive(currentChatMessages1);

        reset(messageListener);
        // Here we merging to equals chats with different currentChatMessages
        messageHolder.onChatReceive(chatItem, chatItem, currentChatMessages2, true);
        verify(messageListener, times(3)).messageAdded(any(), any()); // for 1, 2, 3
        verify(messageListener, times(1)).messageRemoved(any()); // for 8
    }

    @Test
    public void checkMessageAddedCallbackFired() {
        List<MessageImpl> currentMessages = generateCurrentChatInRange(0,10, setOf(8));
        MessageHolder messageHolder = newMessageHolderWithHistory(new History(Collections.<MessageImpl>emptyList()));
        MessageListener messageListener = mock(MessageListener.class);
        MessageTracker messageTracker = messageHolder.newMessageTracker(messageListener);
        MessageTracker.GetMessagesCallback callback = mock(MessageTracker.GetMessagesCallback.class);

        messageHolder.onChatReceive(null, new ChatItem(), currentMessages, true);
        // call this method to set MessageTracker#headMessage pointer first current message '0',
        // because MessageListener fires callbacks only if message placed before headMessage
        messageTracker.getNextMessages(10, callback);

        reset(messageListener);
        MessageImpl newMessage = newCurrentChatMessage(11);
        // Add message to end of chat
        messageHolder.onMessageAdded(newMessage);
        // Verify message added to end
        verify(messageListener, times(1)).messageAdded(null, newMessage);

        reset(messageListener);
        newMessage = newCurrentChatMessage(8); // message with id '8'
        // Add message to middle of chat
        messageHolder.onMessageAdded(newMessage);
        // Verify message added to middle
        MessageImpl beforeMessage = currentMessages.get(8); // message with id '9'
        verify(messageListener, times(1)).messageAdded(beforeMessage, newMessage);
    }


    @Test
    public void checkMessageChangedCallbackFired() {
        List<MessageImpl> currentMessages = generateCurrentChatInRange(0,10, setOf());
        MessageHolder messageHolder = newMessageHolderWithHistory(new History(Collections.<MessageImpl>emptyList()));
        MessageListener messageListener = mock(MessageListener.class);
        MessageTracker messageTracker = messageHolder.newMessageTracker(messageListener);
        MessageTracker.GetMessagesCallback callback = mock(MessageTracker.GetMessagesCallback.class);

        messageHolder.onChatReceive(null, new ChatItem(), currentMessages, true);
        // call this method to set MessageTracker#headMessage pointer first current message '0',
        // because MessageListener fires callbacks only if message placed before headMessage
        messageTracker.getNextMessages(10, callback);

        reset(messageListener);
        MessageImpl originalMessage = currentMessages.get(8);
        MessageImpl editedMessage = newEditedHistoryMessage(originalMessage);
        // Add message to middle of chat
        messageHolder.onMessageChanged(editedMessage);
        // Verify message was changed at position '8'
        verify(messageListener, times(1)).messageChanged(originalMessage, editedMessage);
    }


    @Test
    public void checkMessageDeletedCallbackFired() {
        List<MessageImpl> currentMessages = generateCurrentChatInRange(0,10, setOf());
        MessageHolder messageHolder = newMessageHolderWithHistory(new History(Collections.<MessageImpl>emptyList()));
        MessageListener messageListener = mock(MessageListener.class);
        MessageTracker messageTracker = messageHolder.newMessageTracker(messageListener);
        MessageTracker.GetMessagesCallback callback = mock(MessageTracker.GetMessagesCallback.class);

        messageHolder.onChatReceive(null, new ChatItem(), currentMessages, true);
        // call this method to set MessageTracker#headMessage pointer first current message '0',
        // because MessageListener fires callbacks only if message placed before headMessage
        messageTracker.getNextMessages(10, callback);

        reset(messageListener);
        MessageImpl messageToDelete = currentMessages.get(5);
        // Delete message from middle of chat
        messageHolder.onMessageDeleted(messageToDelete.getServerSideId());
        // Verify message deleted from middle
        verify(messageListener, times(1)).messageRemoved(messageToDelete);
    }

    @Test
    public void checkTest1() {
        List<MessageImpl> currentMessages = generateCurrentChatInRange(0,10, setOf());
        MessageHolder messageHolder = newMessageHolderWithHistory(new History(Collections.emptyList()));
        MessageListener messageListener = mock(MessageListener.class);
        MessageTracker messageTracker = messageHolder.newMessageTracker(messageListener);
        MessageTracker.GetMessagesCallback callback = mock(MessageTracker.GetMessagesCallback.class);

        reset(callback);
        reset(messageListener);
        messageTracker.getNextMessages(10, callback);
        messageHolder.onChatReceive(null, null, Collections.emptyList(), true);
        verify(messageListener, never()).messageAdded(any(), any());
        verify(callback, times(1)).receive(Collections.emptyList());

        reset(messageListener);
        reset(callback);
        messageHolder.onChatReceive(null, new ChatItem(), currentMessages, true);
        verify(messageListener, times(10)).messageAdded(any(), any());
        verify(callback, never()).receive(any());
    }

    @Test
    public void checkListenerIsCalledForHistoryUpdatedMessages() {
        List<MessageImpl> localOldHistoryMessages = generateCurrentChatInRange(0,10, setOf());
        List<MessageImpl> remoteUpdatedHistoryMessages = generateCurrentChatInRange(0,10, setOf());
        List<MessageImpl> editedList = new ArrayList<>();
        editedList.add(newEditedCurrentChatMessage(remoteUpdatedHistoryMessages.get(0)));
        editedList.add(newEditedCurrentChatMessage(remoteUpdatedHistoryMessages.get(1)));
        editedList.add(newEditedCurrentChatMessage(remoteUpdatedHistoryMessages.get(2)));
        ArgumentCaptor<Message> captor = newMessageCaptor();

        MessageHolder messageHolder = newMessageHolderWithHistory(new History(Collections.emptyList()));
        MessageListener messageListener = mock(MessageListener.class);
        MessageTracker messageTracker = messageHolder.newMessageTracker(messageListener);
        MessageTracker.GetMessagesCallback callback = mock(MessageTracker.GetMessagesCallback.class);

        messageTracker.getNextMessages(10, callback);
        messageHolder.onChatReceive(null, new ChatItem(), localOldHistoryMessages, true);

        reset(messageListener);
        reset(callback);
        messageHolder.receiveHistoryUpdate(editedList, setOf(), EMPTY_RUNNABLE);
        verify(messageListener, times(3)).messageChanged(any(), captor.capture());
        checkCapturedMessages(captor.getAllValues(), editedList);
    }

    @Test
    public void checkMirrorsMessages() {
        MessageHolder messageHolder = newMessageHolderWithHistory(new History(Collections.emptyList()));
        MessageListener messageListener = mock(MessageListener.class);
        messageHolder.newMessageTracker(messageListener);

        messageHolder.onChatReceive(null, null, Collections.emptyList(), true);

        MessageSending sending1 = newSendingMessage(1);
        MessageSending sending2 = newSendingMessage(2);
        MessageSending sending4 = newSendingMessage(4);


        messageHolder.onSendingMessage(sending1);
        messageHolder.onSendingMessage(sending2);
        messageHolder.onSendingMessage(sending4);

        MessageImpl message1 = newEditedCurrentChatMessage(sending1);
        MessageImpl message2 = newEditedCurrentChatMessage(sending2);
        MessageImpl message3 = newCurrentChatMessage(3);
        MessageImpl message4 = newEditedCurrentChatMessage(sending4);

        messageHolder.onMessageAdded(message1);
        verify(messageListener, times(1)).messageChanged(sending1, message1);
        messageHolder.onMessageAdded(message4);
        verify(messageListener, times(1)).messageChanged(sending4, message4);
        messageHolder.onMessageAdded(message2);
        verify(messageListener, times(1)).messageChanged(sending2, message2);
        messageHolder.onMessageAdded(message3);
        verify(messageListener, times(1)).messageAdded(message4, message3);
    }
}