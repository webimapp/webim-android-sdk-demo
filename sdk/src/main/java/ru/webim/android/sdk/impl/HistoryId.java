package ru.webim.android.sdk.impl;

import androidx.annotation.NonNull;

public class HistoryId implements TimeMicrosHolder {
    @NonNull
    private final String dbId;
    private final long timeMicros;

    public HistoryId(@NonNull String dbId, long timeMicros) {
        dbId.getClass(); // NPE
        this.dbId = dbId;
        this.timeMicros = timeMicros;
    }

    @NonNull
    public String getDbId() {
        return dbId;
    }

    @Override
    public long getTimeMicros() {
        return timeMicros;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HistoryId historyId = (HistoryId) o;

        return timeMicros == historyId.timeMicros && dbId.equals(historyId.dbId);

    }

    @Override
    public int hashCode() {
        int result = dbId.hashCode();
        result = 31 * result + (int) (timeMicros ^ (timeMicros >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "HistoryId{" +
                "dbId='" + dbId + '\'' +
                ", timeMicros=" + timeMicros +
                '}';
    }
}
