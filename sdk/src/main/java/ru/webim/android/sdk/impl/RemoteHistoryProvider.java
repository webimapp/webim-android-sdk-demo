package ru.webim.android.sdk.impl;

public interface RemoteHistoryProvider {
    /**
    * @param beforeMessageTs timestamp in micros
    * @param callback messages callback
    */
    void requestHistoryBefore(long beforeMessageTs, HistoryBeforeCallback callback);
}
