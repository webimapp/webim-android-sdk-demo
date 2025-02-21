package ru.webim.android.sdk.impl.backend.callbacks;

public interface SurveyQuestionCallback {
    void onSuccess();

    void onFailure(String error);
}
