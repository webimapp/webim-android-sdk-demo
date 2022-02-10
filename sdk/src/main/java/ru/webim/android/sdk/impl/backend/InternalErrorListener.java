package ru.webim.android.sdk.impl.backend;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.webim.android.sdk.NotFatalErrorHandler;

public interface InternalErrorListener {
    void onError(@NonNull String url, @Nullable String error, int httpCode);

    void onNotFatalError(@NonNull NotFatalErrorHandler.NotFatalErrorType error);
}
