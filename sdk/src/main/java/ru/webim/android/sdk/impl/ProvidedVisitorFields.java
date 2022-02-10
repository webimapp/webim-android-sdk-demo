package ru.webim.android.sdk.impl;

import androidx.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ProvidedVisitorFields {
    private final @NonNull String json;
    private final @NonNull String id;

    public ProvidedVisitorFields(@NonNull String json) {
        this(json, new JsonParser().parse(json).getAsJsonObject());
    }

    public ProvidedVisitorFields(@NonNull JsonObject root) {
        this(root.toString(), root);
    }

    private ProvidedVisitorFields(@NonNull String json, @NonNull JsonObject root) {
        this.json = json;
        JsonObject fields = root.getAsJsonObject("fields");
        JsonElement idElem;
        if (fields == null) {
            idElem = root.get("id");
        }
        else {
            idElem = fields.get("id");
        }
        if (idElem == null) {
            throw new IllegalArgumentException("Visitor Fields json must contain 'id' field");
        }
        id = idElem.getAsString();
    }

    @NonNull
    String getJson() {
        return json;
    }

    @NonNull
    String getId() {
        return id;
    }
}
