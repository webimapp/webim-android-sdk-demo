package ru.webim.android.sdk.impl.items;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Nikita Kaberov on 14.02.19.
 */
public class HistoryRevisionItem {
    @SerializedName("revision")
    private String revision;

    public HistoryRevisionItem() {
        // Need for Gson No-args fix
    }

    public String getRevision() {
        return revision;
    }
}
