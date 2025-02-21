package ru.webim.android.sdk.impl.items.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ru.webim.android.sdk.impl.items.delta.DeltaFullUpdate;
import ru.webim.android.sdk.impl.items.delta.DeltaItem;

final public class DeltaResponse extends ErrorResponse {
    @SerializedName("revision")
    private Long revision;
    @SerializedName("fullUpdate")
    private DeltaFullUpdate fullUpdate;
    @SerializedName("deltaList")
    private List<DeltaItem<?>> deltaList;

    public Long getRevision() {
        return revision;
    }

    public DeltaFullUpdate getFullUpdate() {
        return fullUpdate;
    }

    public List<DeltaItem<?>> getDeltaList() {
        return deltaList;
    }
}
