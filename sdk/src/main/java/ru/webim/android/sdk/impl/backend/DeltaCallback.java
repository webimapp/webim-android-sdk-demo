package ru.webim.android.sdk.impl.backend;

import androidx.annotation.NonNull;

import java.util.List;

import ru.webim.android.sdk.impl.items.delta.DeltaFullUpdate;
import ru.webim.android.sdk.impl.items.delta.DeltaItem;

public interface DeltaCallback {
    void onFullUpdate(@NonNull DeltaFullUpdate fullUpdate);

    void onDeltaList(@NonNull List<DeltaItem> list);
}
