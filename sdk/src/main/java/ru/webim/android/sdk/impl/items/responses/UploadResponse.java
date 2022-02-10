package ru.webim.android.sdk.impl.items.responses;

import com.google.gson.annotations.SerializedName;

import ru.webim.android.sdk.impl.items.FileParametersItem;

public class UploadResponse extends DefaultResponse {
    @SerializedName("data")
    private FileParametersItem data;

    public UploadResponse() {

    }

    public FileParametersItem getData() {
        return data;
    }
}
