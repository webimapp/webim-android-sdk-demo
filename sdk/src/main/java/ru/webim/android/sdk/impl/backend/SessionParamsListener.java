package ru.webim.android.sdk.impl.backend;

import androidx.annotation.NonNull;

public interface SessionParamsListener {
    void onSessionParamsChanged(@NonNull String visitorJson,
                                @NonNull String sessionId,
                                @NonNull AuthData pageId);
}
