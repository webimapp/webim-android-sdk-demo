package ru.webim.android.sdk.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.webim.android.sdk.Operator;

public class OperatorImpl implements Operator {
    private final @NonNull Operator.Id id;
    private final @NonNull String name;
    private final @Nullable String avatarUrl;
    private final @Nullable String title;
    private final @Nullable String info;

    public OperatorImpl(@NonNull Id id, @NonNull String name, @Nullable String avatarUrl, @Nullable String title, @Nullable String info) {
        id.getClass(); // NPE
        name.getClass(); // NPE
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.title = title;
        this.info = info;
    }

    @NonNull
    @Override
    public Id getId() {
        return id;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Operator) {
            Operator operator = (Operator) obj;
            return operator.getId().equals(this.getId());
        } else {
            return false;
        }
    }
}
