package ru.webim.android.sdk.impl;

import androidx.annotation.NonNull;

import ru.webim.android.sdk.Message;
import ru.webim.android.sdk.Operator;

import java.util.UUID;

public class StringId {

    @NonNull
    private final String id;

    private StringId(@NonNull String id) {
        id.getClass(); // NPE
        this.id = id;
    }

    public String getInternal() {
        return id;
    }

    @Override
    public String toString() {
        return getInternal();
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || !(o == null || getClass() != o.getClass()) && id.equals(((StringId) o).id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static String generateClientSide() {
        UUID clientSideId = UUID.randomUUID();
        return clientSideId.toString();
    }

    public static Message.Id generateForMessage() {
        return new StringMessageId(generateClientSide());
    }

    public static Message.Id forMessage(@NonNull String id) {
        return new StringMessageId(id);
    }

    public static Operator.Id forOperator(@NonNull String id) {
        return new StringOperatorId(id);
    }

    private static class StringMessageId extends StringId implements Message.Id {
        public StringMessageId(@NonNull String clientSideId) {
            super(clientSideId);
        }
    }

    private static class StringOperatorId extends StringId implements Operator.Id {
        public StringOperatorId(@NonNull String clientSideId) {
            super(clientSideId);
        }
    }
}
