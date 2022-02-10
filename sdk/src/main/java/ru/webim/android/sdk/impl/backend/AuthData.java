package ru.webim.android.sdk.impl.backend;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AuthData {
    @NonNull
    private final String pageId;
    @Nullable
    private final String authToken;

    public AuthData(@NonNull String pageId, @Nullable String authToken) {
        pageId.getClass(); //NPE
        this.pageId = pageId;
        this.authToken = authToken;
    }

    @NonNull
    public String getPageId() {
        return pageId;
    }

    @Nullable
    public String getAuthToken() {
        return authToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AuthData authData = (AuthData) o;

        return pageId.equals(authData.pageId)
                && (authToken != null
                ? authToken.equals(authData.authToken)
                : authData.authToken == null);

    }

    @Override
    public int hashCode() {
        int result = pageId.hashCode();
        result = 31 * result + (authToken != null ? authToken.hashCode() : 0);
        return result;
    }
}
