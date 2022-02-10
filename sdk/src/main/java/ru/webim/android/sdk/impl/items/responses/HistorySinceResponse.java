package ru.webim.android.sdk.impl.items.responses;

import com.google.gson.annotations.SerializedName;
import ru.webim.android.sdk.impl.items.MessageItem;

import java.util.List;

public class HistorySinceResponse extends ErrorResponse {
    @SerializedName("result")
    private String result;
    @SerializedName("data")
    private HistoryResponseData data;

    public static class HistoryResponseData {
        @SerializedName("hasMore")
        private Boolean hasMore;
        @SerializedName("revision")
        private String revision;
        @SerializedName("messages")
        private List<MessageItem> messages;

        public Boolean getHasMore() {
            return hasMore;
        }

        public String getRevision() {
            return revision;
        }

        public List<MessageItem> getMessages() {
            return messages;
        }
    }

    public HistoryResponseData getData() {
        return data;
    }
}
