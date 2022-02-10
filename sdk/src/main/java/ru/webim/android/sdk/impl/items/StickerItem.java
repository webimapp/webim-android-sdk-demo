package ru.webim.android.sdk.impl.items;

import com.google.gson.annotations.SerializedName;

public class StickerItem {
    @SerializedName("stickerId")
    private int stickerId;

    public int getStickerId() {
        return stickerId;
    }
}
