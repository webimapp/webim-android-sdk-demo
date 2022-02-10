package ru.webim.android.sdk.impl.items.responses;

import com.google.gson.annotations.SerializedName;
import ru.webim.android.sdk.impl.items.MessageItem;

import java.util.List;

public class SearchResponse extends ErrorResponse {
    @SerializedName("result")
    private String result;
    @SerializedName("data")
    private SearchResponseData data;

    public SearchResponse() {
    }

    public String getResult() {
        return result;
    }

    public SearchResponseData getData() {
        return data;
    }

    public static class SearchResponseData {
        @SerializedName("count")
        private int count;
        @SerializedName("items")
        private List<MessageItem> messages;

        public int getCount() {
            return count;
        }

        public List<MessageItem> getMessages() {
            return messages;
        }
    }
}
