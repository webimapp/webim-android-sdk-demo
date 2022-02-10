package ru.webim.android.sdk.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.webim.android.sdk.UploadedFile;

import org.jetbrains.annotations.NotNull;

public class UploadedFileImpl implements UploadedFile {

    private long size;
    private String guid;
    private String contentType;
    private String filename;
    private String visitorId;
    private String clientContentType;
    private UploadedFileImage uploadedFileImage;

    public UploadedFileImpl(long size,
                            @NonNull String guid,
                            @NonNull String contentType,
                            @NonNull String filename,
                            @NonNull String visitorId,
                            @NonNull String clientContentType,
                            @Nullable UploadedFileImage uploadedFileImage
    ) {
        this.size = size;
        this.guid = guid;
        this.contentType = contentType;
        this.filename = filename;
        this.visitorId = visitorId;
        this.clientContentType = clientContentType;
        this.uploadedFileImage = uploadedFileImage;
    }

    @Override
    public long getSize() {
        return size;
    }

    @NonNull
    @Override
    public String getGuid() {
        return guid;
    }

    @NonNull
    @Override
    public String getFileName() {
        return filename;
    }

    @Nullable
    @Override
    public String getContentType() {
        return contentType;
    }

    @NonNull
    @Override
    public String getVisitorId() {
        return visitorId;
    }

    @NonNull
    @Override
    public String getClientContentType() {
        return clientContentType;
    }

    @NonNull
    @Override
    public String toString() {

        return "{\"client_content_type\":\"" + clientContentType +
            "\",\"visitor_id\":\"" + visitorId +
            "\",\"filename\":\"" + filename  +
            "\",\"content_type\":\"" + contentType +
            "\",\"guid\":\"" + guid +
            "\",\"size\":"+ size +
            ",\"image\":"+ (uploadedFileImage == null ? "null" : uploadedFileImage.toString()) +
            "}";
    }

    static class UploadedFileImage {
        private int width;
        private int height;

        public UploadedFileImage(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        @NotNull
        @Override
        public String toString() {
            return "{\"size\":{" +
                "\"width\":" + width +
                ",\"height\":" + height +
                "}}";
        }
    }
}

