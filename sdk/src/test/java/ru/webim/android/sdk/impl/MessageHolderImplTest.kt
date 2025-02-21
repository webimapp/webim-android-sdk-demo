package ru.webim.android.sdk.impl

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import ru.webim.android.sdk.Message
import ru.webim.android.sdk.MessageListener
import ru.webim.android.sdk.MessageTracker
import ru.webim.android.sdk.MessageTracker.GetMessagesCallback
import ru.webim.android.sdk.impl.items.ChatItem
import kotlin.math.max

@RunWith(MockitoJUnitRunner::class)
class MessageHolderImplTest {
    private val messageCaptor: KArgumentCaptor<Message>
        get() = argumentCaptor()
    private val listCaptor: KArgumentCaptor<List<Message>>
        get() = argumentCaptor()

    fun mockers(messageHolder: MessageHolder): Triple<MessageTracker, MessageListener, GetMessagesCallback> {
        val listener = Mockito.mock(MessageListener::class.java)
        val callback = Mockito.mock(GetMessagesCallback::class.java)
        val tracker = messageHolder.newMessageTracker(listener)
        return Triple(tracker, listener, callback)
    }

    @Test
    fun checkHistoryGeneration() {
        val ids1 = generateChat(0, 10, isHistory = true).map { it.serverSideId.toInt() }
        val ids3 = generateChat(20, 30, isHistory = true).map { it.serverSideId.toInt() }

        assertEquals(ids1, (0..9).toList())
        assertEquals(ids3, (20..29).toList())
    }

    @Test
    fun respondCurrentChatMessagesImmediately() {
        val currentMsgs = generateChat(0, 10)
        val holder = messageHolder()
        val (tracker, listener, callback) = mockers(holder)

        holder.onChatReceive(null, ChatItem(), currentMsgs)
        tracker.getNextMessages(10, callback)

        verify(callback, times(1)).receive(currentMsgs)
    }

    @Test
    fun checkGetNextMessages() {
        val currentMsgs = generateChat(0, 10)
        val historyMsgs = generateChat(0, 10, isHistory = true)
        val holder = messageHolder()
        val (tracker, listener, callback) = mockers(holder)

        // This method caches callback
        tracker.getNextMessages(10, callback)
        // Callback will be cached because local storage is empty.
        verify(callback, never()).receive(any())
        reset(callback)

        // Our cached callback will be fired.
        holder.onChatReceive(null, ChatItem(), currentMsgs)
        verify(callback, times(1)).receive(currentMsgs)
        reset(listener)

        // History messages will be saved to local storage and merged with currentMessages
        holder.receiveHistoryUpdate(historyMsgs, setOf()) {}
        verify(listener, times(currentMsgs.size)).messageChanged(any(), any())
    }

    @Test
    fun checkGetAllMessages() {
        val history1 = generateChat(0, 10, isHistory = true)
        val history2 = generateChat(10, 20, isHistory = true)
        val currentMsgs = generateChat(10, 20)
        val holder = messageHolder()
        val (tracker, listener, callback) = mockers(holder)

        // This method doesn't cache callback, it just gets all messages from local storage
        tracker.getAllMessages(callback)
        // Local storage is empty, so we get empty list
        verify(callback, times(1)).receive(emptyList())
        holder.onChatReceive(null, ChatItem(), currentMsgs)
        // history1 will be saved to local storage
        holder.receiveHistoryUpdate(history1, setOf()) {}
        // history2 will be saved to local storage
        holder.receiveHistoryUpdate(history2, setOf()) {}
        reset(callback)

        tracker.getAllMessages(callback)
        verify(callback, times(1)).receive(history1 + history2)
    }

    @Test
    fun checkGetLastMessages() {
        val currentChatMessages = generateChat(0, 10)
        val history1 = generateChat(0, 10, isHistory = true)
        val history2 = generateChat(10, 20)
        val holder = messageHolder()
        val (tracker, listener, callback) = mockers(holder)

        // This method caches callback
        tracker.getLastMessages(10, callback)
        // Local storage is empty, so callback is cached and will not be fired
        verify(callback, never()).receive(any())
        reset(callback)

        holder.onChatReceive(null, ChatItem(), currentChatMessages)
        // Fire cached callback
        verify(callback, times(1)).receive(currentChatMessages)
        reset(listener)

        holder.receiveHistoryUpdate(history1, setOf()) {}
        verify(listener, times(history1.size)).messageChanged(any(), any())
        holder.receiveHistoryUpdate(history2, setOf()) {}
        verify(listener, times(history2.size)).messageAdded(isNull(), any())
        reset(callback)

        tracker.getLastMessages(10, callback)
        verify(callback, times(1)).receive(history2)
    }

    @Test
    fun checkResetTo() {
        val currentMsgs = generateChat(0, 10)
        val holder = messageHolder()
        val (tracker, listener, callback) = mockers(holder)

        holder.onChatReceive(null, ChatItem(), currentMsgs)
        tracker.getNextMessages(8, callback)
        verify(callback, times(1)).receive(currentMsgs.subList(2, 10))

        // Now headMessage is set to message with index '2', let's reset currentMessages to '5'
        reset(listener)
        tracker.resetTo(currentMsgs[5])
        // Check that for messages from '2' to '5' was called callback
        verify(listener, times(3)).messageRemoved(any())
        reset(callback)

        tracker.getNextMessages(5, callback)
        // Here we must get messages from '5' to '0'
        verify(callback, times(1)).receive(currentMsgs.subList(0, 5))
        reset(listener)

        tracker.resetTo(currentMsgs[9])
        verify(listener, times(9)).messageRemoved(any())
        reset(callback)

        tracker.getNextMessages(10, callback)
        verify(callback, times(1)).receive(currentMsgs.subList(0, 9))
    }

    @Test
    fun checkMergeCurrentChatWith() {
        val current1 = generateChat(0, 10, setOf(1, 2, 3))
        val current2 = generateChat(0, 10, setOf(8))
        val holder = messageHolder()
        val (tracker, listener, callback) = mockers(holder)

        // set these private fields, to make chat items equals
        val chatItem = ChatItem().apply {
            setPrivateField("id", "1id")
            setPrivateField("clientSideId", "1csi")
        }
        assertEquals(chatItem, chatItem)

        holder.onChatReceive(null, chatItem, current1)
        tracker.getNextMessages(10, callback)
        verify(callback, times(1)).receive(current1)
        reset(listener)
        // Here we merging to equals chats with different currentChatMessages
        holder.onChatReceive(chatItem, chatItem, current2)
        // for 1, 2, 3
        verify(listener, times(3)).messageAdded(any(), any())
        // for 8
        verify(listener, times(1)).messageRemoved(any())
    }

    @Test
    fun checkMessageAddedCallbackFired() {
        val currentMessages = generateChat(0, 10, setOf(8))
        val holder = messageHolder()
        val (tracker, listener, callback) = mockers(holder)

        holder.onChatReceive(null, ChatItem(), currentMessages)
        // call this method to set MessageTracker#headMessage pointer first current message '0',
        // because MessageListener fires callbacks only if message placed before headMessage
        tracker.getNextMessages(10, callback)
        reset(listener)
        var newMessage: MessageImpl? = message(11)
        // Add message to end of chat
        holder.onMessageAdded(newMessage!!)
        // Verify message added to end
        verify(listener, times(1)).messageAdded(null, newMessage)
        reset(listener)
        newMessage = message(8) // message with id '8'
        // Add message to middle of chat
        holder.onMessageAdded(newMessage)
        // Verify message added to middle
        val beforeMessage = currentMessages[8] // message with id '9'
        verify(listener, times(1)).messageAdded(beforeMessage, newMessage)
    }

    @Test
    fun checkMessageChangedCallbackFired() {
        val currentMsgs = generateChat(0, 10)
        val holder = messageHolder()
        val (tracker, listener, callback) = mockers(holder)

        holder.onChatReceive(null, ChatItem(), currentMsgs)
        // call this method to set MessageTracker#headMessage pointer first current message '0',
        // because MessageListener fires callbacks only if message placed before headMessage
        tracker.getNextMessages(10, callback)
        verify(callback).receive(currentMsgs)
        reset(callback)

        val originalMessage = currentMsgs[8]
        val editedMessage: MessageImpl = message(8, isEdited = true)
        // Add message to middle of chat
        holder.onMessageChanged(editedMessage)
        // Verify message was changed at position '8'
        verify(listener).messageChanged(originalMessage, editedMessage)
    }

    @Test
    fun checkMessageDeletedCallbackFired() {
        val currentMsgs = generateChat(0, 10)
        val holder = messageHolder()
        val (tracker, listener, callback) = mockers(holder)

        holder.onChatReceive(null, ChatItem(), currentMsgs)
        // call this method to set MessageTracker#headMessage pointer first current message '0',
        // because MessageListener fires callbacks only if message placed before headMessage
        tracker.getNextMessages(10, callback)
        reset(listener)
        val messageToDelete = currentMsgs[5]
        // Delete message from middle of chat
        holder.onMessageDeleted(messageToDelete.serverSideId)
        // Verify message deleted from middle
        verify(listener, times(1)).messageRemoved(messageToDelete)
    }

    @Test
    fun checkMessagesAdded() {
        val currentMsgs = generateChat(0, 10)
        val holder = messageHolder()
        val (tracker, listener, callback) = mockers(holder)

        tracker.getNextMessages(10, callback)
        verify(callback, never()).receive(any())
        reset(callback)

        holder.onChatReceive(null, null, emptyList())
        verify(listener, never()).messageAdded(any(), any())
        verify(callback).receive(emptyList())
        reset(listener)
        reset(callback)

        holder.onChatReceive(null, ChatItem(), currentMsgs)
        verify(listener, times(10)).messageAdded(anyOrNull(), any())
        verify(callback, never()).receive(any())
    }

    @Test
    fun checkListenerIsCalledForHistoryUpdatedMessages() {
        val localOldHistoryMessages = generateChat(0, 10)
        val remoteUpdatedHistoryMessages = generateChat(0, 10)
        val editedList: MutableList<MessageImpl> = mutableListOf()
        editedList.add(message(0, isEdited = true))
        editedList.add(message(1, isEdited = true))
        editedList.add(message(2, isEdited = true))
        val holder = messageHolder()
        val (tracker, listener, callback) = mockers(holder)

        tracker.getNextMessages(10, callback)
        holder.onChatReceive(null, ChatItem(), localOldHistoryMessages)
        reset(listener)
        reset(callback)

        holder.receiveHistoryUpdate(editedList, setOf()) {}
        val captor = messageCaptor
        verify(listener, times(3)).messageChanged(any(), captor.capture())
        assertArrayEquals(editedList.toTypedArray(), captor.allValues.toTypedArray())
    }

    @Test
    fun checkMirrorsMessages() {
        val holder = messageHolder()
        val (tracker, listener, callback) = mockers(holder)

        holder.onChatReceive(null, null, emptyList())
        val sending1 = sendingMessage(1)
        val sending2 = sendingMessage(2)
        val sending4 = sendingMessage(4)

        holder.onSendingMessage(sending1)
        holder.onSendingMessage(sending2)
        holder.onSendingMessage(sending4)

        val message1 = message(1)
        val message2 = message(2)
        val message3 = message(3)
        val message4 = message(4)

        holder.onMessageAdded(message1)
        verify(listener).messageChanged(sending1, message1)
        reset(listener)

        holder.onMessageAdded(message4)
        verify(listener).messageChanged(sending4, message4)
        reset(listener)

        holder.onMessageAdded(message2)
        verify(listener).messageChanged(sending2, message2)
        reset(listener)

        holder.onMessageAdded(message3)
        verify(listener).messageAdded(message4, message3)
    }

    private fun generateChat(from: Int, to: Int, expect: Set<Int> = setOf(), isHistory: Boolean = false): MutableList<MessageImpl> {
        val messages: MutableList<MessageImpl> = mutableListOf()
        for (index in from until to) {
            if (expect.contains(index)) continue
            messages.add(message(index, isHistory))
        }
        return messages
    }

    private fun message(index: Int, isEdited: Boolean = false, isHistory: Boolean = false) = MessageImpl(
        "",
        StringId.forMessage(index.toString()),
        null,
        StringId.forOperator("op"),
        "",
        "",
        Message.Type.OPERATOR,
        if (!isEdited) "text $index" else "edited $index",
        index * 1000L,
        index.toString(),
        null,
        isHistory,
        null,
        false,
        true,
        false,
        isEdited,
        null,
        null,
        null,
        null,
        null,
        false,
        false
    )

    private fun sendingMessage(index: Int) = MessageSending(
        "",
        StringId.forMessage(index.toString()),
        "",
        Message.Type.VISITOR,
        index.toString(),
        index * 1000L,
        null,
        null,
        null
    )

    private fun messageHolder(serverHistory: List<MessageImpl> = listOf(), localHistory: List<MessageImpl> = listOf()): MessageHolderImpl {
        return MessageHolderImpl(AccessChecker.EMPTY, History(serverHistory), MemoryHistoryStorage(localHistory), false)
    }

    private fun Any.setPrivateField(privateFieldName: String, privateFieldValue: Any) {
        try {
            val field = javaClass.getDeclaredField(privateFieldName)
            field.isAccessible = true
            field[this] = privateFieldValue
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    internal class History(var history: List<MessageImpl>) :
        RemoteHistoryProvider {
        override fun requestHistoryBefore(beforeMessageTs: Long, callback: HistoryBeforeCallback) {
            var before = -1
            for (i in history.indices.reversed()) {
                val historyMessage = history[i]
                if (historyMessage.getTimeMicros() == beforeMessageTs) {
                    before = i
                    break
                } else if (historyMessage.getTimeMicros() < beforeMessageTs) {
                    before = i + 1
                    break
                }
            }
            val after = max(0, before - 100)
            callback.onSuccess(
                if (before <= 0) emptyList() else history.subList(after, before),
                after != 0
            )
        }
    }
}