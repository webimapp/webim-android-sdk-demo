package ru.webim.android.sdk.impl.backend;

import androidx.annotation.NonNull;

public interface FAQClient {
    void start();

    void pause();

    void resume();

    void stop();

    @NonNull
    FAQActions getActions();
}
