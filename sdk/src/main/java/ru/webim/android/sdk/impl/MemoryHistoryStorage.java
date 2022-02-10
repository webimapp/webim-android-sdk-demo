package ru.webim.android.sdk.impl;

import androidx.annotation.NonNull;

import ru.webim.android.sdk.Message;
import ru.webim.android.sdk.MessageTracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableList;

public class MemoryHistoryStorage implements HistoryStorage {
    private static final Comparator<TimeMicrosHolder> MSG_TS_COMP = new Comparator<TimeMicrosHolder>() {
        @Override
        public int compare(TimeMicrosHolder lhs, TimeMicrosHolder rhs) {
            return InternalUtils.compare(lhs.getTimeMicros(), rhs.getTimeMicros());
        }
    };
    private final int majorVersion = (int) (System.currentTimeMillis() % (long) Integer.MAX_VALUE);
    private final List<MessageImpl> historyMessages = new ArrayList<>();
    private boolean isReachedEndOfHistory;
    private long readBeforeTimestamp;
    private ReadBeforeTimestampListener readBeforeTimestampListener = new ReadBeforeTimestampListener() {
        @Override
        public void onTimestampChanged(long timestamp) {
            if (readBeforeTimestamp < timestamp) {
                readBeforeTimestamp = timestamp;
            }
        }
    };

    public MemoryHistoryStorage() { }

    MemoryHistoryStorage(long readBeforeTimestamp) {
        this.readBeforeTimestamp = readBeforeTimestamp;
    }

    public MemoryHistoryStorage(List<MessageImpl> toAdd) { // for testings
        historyMessages.addAll(toAdd);
    }

    @Override
    public int getMajorVersion() {
        return majorVersion;
    }

    @Override
    public void setReachedEndOfRemoteHistory(boolean isReachedEndOfRemoteHistory) { }

    @Override
    public void receiveHistoryUpdate(@NonNull List<? extends MessageImpl> messages,
                                     @NonNull Set<String> deleted,
                                     @NonNull UpdateHistoryCallback callback) {
        deleteFromHistory(deleted, callback);
        mergeHistoryChanges(messages, callback);
        callback.endOfBatch();
    }

    private void deleteFromHistory(Set<String> deleted, UpdateHistoryCallback callback) {
        for (Iterator<MessageImpl> it = historyMessages.iterator(); it.hasNext(); ) {
            MessageImpl msg = it.next();
            if (deleted.contains(msg.getServerSideId())) {
                it.remove();
                callback.onHistoryDeleted(msg.getServerSideId());
            }
        }
    }

    private void mergeHistoryChanges(List<? extends MessageImpl> messages,
                                     UpdateHistoryCallback callback) {
        boolean isFirstHistory = historyMessages.size() == 0;
        for (MessageImpl msg : messages) {
            if (msg != null) {
                if (msg.getTimeMicros() <= readBeforeTimestamp || readBeforeTimestamp == -1) {
                    msg.setReadByOperator(true);
                }
                int ind = Collections.binarySearch(historyMessages, msg, MSG_TS_COMP);
                if (ind >= 0) {
                    historyMessages.set(ind, msg);
                    callback.onHistoryChanged(msg);
                } else {
                    int beforeInd = -ind - 1;
                    if (beforeInd == 0 && !isFirstHistory && !isReachedEndOfHistory) {
                        continue; // The change is before first message
                    }
                    MessageImpl before = null;
                    if (beforeInd < historyMessages.size()) {
                        before = historyMessages.get(beforeInd);
                    }
                    historyMessages.add(beforeInd, msg);
                    callback.onHistoryAdded(before != null ? before.getServerSideId() : null, msg);
                }
            }
        }
    }

    @Override
    public void receiveHistoryBefore(@NonNull List<? extends MessageImpl> messages,
                                     boolean hasMore) {
        if (!hasMore) {
            isReachedEndOfHistory = true;
        }
        historyMessages.addAll(0, messages);
    }

    @Override
    public void getLatest(int limit, @NonNull MessageTracker.GetMessagesCallback callback) {
        respondMessages(callback, historyMessages, limit);
    }

    @Override
    public void getFull(@NonNull MessageTracker.GetMessagesCallback callback) {
        callback.receive(historyMessages);
    }

    @Override
    public void getBefore(long beforeTimestamp,
                          int limit,
                          @NonNull MessageTracker.GetMessagesCallback callback) {
        TimeMicrosHolder timeMicrosHolder = () -> beforeTimestamp;
        int ind = Collections.binarySearch(historyMessages, timeMicrosHolder, MSG_TS_COMP);
        if (ind <= 0) {
            callback.receive(Collections.<Message>emptyList());
            return;
        }
        respondMessages(callback, historyMessages, ind, limit);
    }

    @Override
    public void clearHistory() {
        historyMessages.clear();
    }

    @Override
    public ReadBeforeTimestampListener getReadBeforeTimestampListener() {
        return readBeforeTimestampListener;
    }

    private static void respondMessages(MessageTracker.GetMessagesCallback callback,
                                        List<? extends Message> messages,
                                        int limit) {
        callback.receive(messages.size() == 0
                ? Collections.<Message>emptyList()
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
}
