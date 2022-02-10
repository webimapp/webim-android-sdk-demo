package ru.webim.android.sdk.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.webim.android.sdk.MessageTracker;

import java.util.List;
import java.util.Set;

public interface HistoryStorage {

    /**
     * On this value change history will re-requested
     */
    int getMajorVersion();

    void setReachedEndOfRemoteHistory(boolean isReachedEndOfRemoteHistory);

    void receiveHistoryUpdate(@NonNull List<? extends MessageImpl> messages,
                              @NonNull Set<String> deleted,
                              @NonNull UpdateHistoryCallback callback);

    void receiveHistoryBefore(@NonNull List<? extends MessageImpl> messages, boolean hasMore);

    void getLatest(int limit, @NonNull MessageTracker.GetMessagesCallback callback);

    void getFull(@NonNull MessageTracker.GetMessagesCallback callback);

    void getBefore(long beforeTimestamp,
                   int limit,
                   @NonNull MessageTracker.GetMessagesCallback callback);

    void clearHistory();

    ReadBeforeTimestampListener getReadBeforeTimestampListener();

    interface UpdateHistoryCallback {
        void onHistoryChanged(@NonNull MessageImpl message);

        void onHistoryAdded(@Nullable String beforeServerId, @NonNull MessageImpl message);

        void onHistoryDeleted(String id);

        void endOfBatch();
    }

    interface ReadBeforeTimestampListener {
        void onTimestampChanged(long timestamp);
    }
}
