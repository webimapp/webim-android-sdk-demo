package ru.webim.android.sdk.impl.backend;

public interface SurveyFinishCallback {
    void onSuccess();

    void onFailure(String error);
}
