package ru.webim.android.sdk.impl.backend.callbacks;

public interface SurveyFinishCallback {
    void onSuccess();

    void onFailure(String error);
}
