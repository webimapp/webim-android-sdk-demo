package ru.webim.android.sdk.impl.items;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class FileParametersItem {
    @SerializedName("size")
    private long size;
    @SerializedName("guid")
    private String guid;
    @SerializedName("content_type")
    private String contentType;
    @SerializedName("filename")
    private String filename;
    @SerializedName("image")
    private WMImageParams image;
    @SerializedName("visitor_id")
    private String visitorId;
    @SerializedName("client_content_type")
    private String clientContentType;

    public long getSize() {
        return size;
    }

    public String getGuid() {
        return guid;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFilename() {
        return filename;
    }

    public WMImageParams getImageParams() {
        return image;
    }

    public String getVisitorId() { return visitorId; }

    public String getClientContentType() { return clientContentType; }

    @NonNull
    @Override
    public String toString() {

        return "{\"client_content_type\":\"" + getClientContentType() +
                "\",\"visitor_id\":\"" + getVisitorId() +
                "\",\"filename\":\"" + getFilename()  +
                "\",\"content_type\":\"" + getContentType() +
                "\",\"guid\":\"" + getGuid() +
                "\",\"size\":"+ getSize() +
                ",\"image\":"+ (getImageParams() == null ? "null" : getImageParams().toString()) +
                "}";
    }

    public static class WMImageParams {

        @SerializedName("size")
        private WMImageSize size;

        public WMImageSize getSize() {
            return size;
        }

        @NonNull
        @Override
        public String toString() {
            return "{\"size\":" + size.toString() + "}";
        }

        public static class WMImageSize {

            @SerializedName("width")
            private int width;
            @SerializedName("height")
            private int height;

            public int getWidth() {
                return width;
            }

            public int getHeight() {
                return height;
            }

            @NonNull
            @Override
            public String toString() {
                return "{" +
                    "\"width\":" + width +
                    ",\"height\":" + height +
                    "}";
            }
        }
    }

}
