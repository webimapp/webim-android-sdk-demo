package ru.webim.android.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Abstract file that was uploaded to the server ({@link MessageStream#uploadFileToServer}).
 * This object is returned in the callback of the method {@link MessageStream#uploadFileToServer}.
 */
public interface UploadedFile {
    /**
     * @return file size in bytes
     */
    long getSize();

    /**
     * @return guid of a file
     */
    @NonNull
    String getGuid();

    /**
     * @return name of a file
     */
    @NonNull
    String getFileName();

    /**
     * @return MIME-type of a file
     */
    @Nullable String getContentType();

    /**
     * @return visitor Id of a file
     */
    @NonNull
    String getVisitorId();

    /**
     * @return MIME-type of a file
     */
    @NonNull
    String getClientContentType();
}
