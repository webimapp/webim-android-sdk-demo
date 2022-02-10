package ru.webim.android.sdk.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.webim.android.sdk.WebimError;

public class WebimErrorImpl<T extends Enum> implements WebimError<T> {
    private final @NonNull T errorType;
    private final @Nullable String errorString;

    public WebimErrorImpl(@NonNull T errorType, @Nullable String errorString) {
        this.errorType = errorType;
        this.errorString = errorString;
    }

    @NonNull
    @Override
    public T getErrorType() {
        return errorType;
    }

    @NonNull
    @Override
    public String getErrorString() {
        return errorString == null ? errorType.toString() : errorString;
    }
}
