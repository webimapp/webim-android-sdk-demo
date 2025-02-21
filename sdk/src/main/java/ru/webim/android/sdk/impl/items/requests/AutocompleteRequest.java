package ru.webim.android.sdk.impl.items.requests;

import com.google.gson.annotations.SerializedName;

public class AutocompleteRequest {
    @SerializedName("text")
    private String text;

    public AutocompleteRequest() {
    }

    public AutocompleteRequest(String text) {
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
