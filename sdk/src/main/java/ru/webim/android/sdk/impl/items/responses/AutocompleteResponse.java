package ru.webim.android.sdk.impl.items.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ru.webim.android.sdk.impl.items.SuggestionItem;

public class AutocompleteResponse extends ErrorResponse {
    @SerializedName("suggestions")
    private List<SuggestionItem> suggestionItems;

    public List<SuggestionItem> getSuggestions() {
        return suggestionItems;
    }
}
