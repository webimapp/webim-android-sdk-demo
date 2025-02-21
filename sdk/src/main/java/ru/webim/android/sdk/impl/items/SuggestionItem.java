package ru.webim.android.sdk.impl.items;

import com.google.gson.annotations.SerializedName;

public class SuggestionItem {
    @SerializedName("tagId")
    private String tagId;
    @SerializedName("suggestId")
    private String suggestId;
    @SerializedName("text")
    private String text;

    public String getTagId() {
        return tagId;
    }

    public String getSuggestId() {
        return suggestId;
    }

    public String getText() {
        return text;
    }
}
