package ru.webim.android.sdk.impl.backend.callbacks;

public interface SendOrDeleteMessageInternalCallback {
    void onSuccess(String response);

    void onFailure(String error);
}
