package ru.webim.chatview.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;

import ru.webim.android.sdk.Message;
import ru.webim.android.sdk.MessageStream;
import ru.webim.android.sdk.WebimError;
import ru.webim.android.sdk.WebimSession;
import ru.webim.chatview.R;

public class FileHelper {
    private HandlerThread handlerThread;
    private Handler workerHandler;
    private Handler mainHandler;
    private Context context;
    private WebimSession session;

    private static final String TAG = FileHelper.class.getSimpleName();

    public static void openFilePicker(ActivityResultLauncher<Intent> resultLauncher) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            if (resultLauncher != null) {
                resultLauncher.launch(intent);
            }
        } catch (android.content.ActivityNotFoundException e) {
        }
    }

    public void sendFileWithNewTemp(Uri uri) {
        workerHandler.post(() -> sendFileWithNewTempInternal(uri));
    }

    public void sendFileWithDescriptor(Uri uri) {
        sendFileWithDescriptor(uri, "r");
    }

    public void sendFileWithDescriptor(Uri uri, String mode) {
        workerHandler.post(() -> sendFileDescriptorInternal(uri, mode));
    }

    public void startWork(Context context, WebimSession session) {
        this.context = context;
        this.session = session;

        handlerThread = new HandlerThread(this.getClass().getSimpleName());
        handlerThread.start();
        workerHandler = new Handler(handlerThread.getLooper());
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void stopWork() {
        handlerThread.quit();
        context = null;
        session = null;
    }


    private void sendFileDescriptorInternal(Uri uri, String mode) {
        ContentResolver contentResolver = context.getContentResolver();
        try {
            ParcelFileDescriptor descriptor = contentResolver.openFileDescriptor(uri, mode);
            String mime = resolveMimeType(uri);
            String filename = resolveFilename(uri);

            MessageStream.SendFileCallback callback = new MessageStream.SendFileCallback() {
                @Override
                public void onProgress(@NonNull Message.Id id, long sentBytes) {
                }

                @Override
                public void onSuccess(@NonNull Message.Id id) {
                    try {
                        descriptor.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NonNull Message.Id id, @NonNull WebimError<SendFileError> error) {
                    try {
                        descriptor.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    handleFileError(error);
                }
            };
            mainHandler.post(() -> session.getStream().sendFile(descriptor.getFileDescriptor(), filename, mime, callback));
        } catch (Exception e) {
            mainHandler.post(() -> showErrorTost(context.getString(R.string.file_upload_failed_unknown)));
        }
    }

    public String resolveMimeType(Uri uri) {
        String mime = null;
        String contentScheme = "content";
        String fileScheme = "file";

        if (uri.getScheme().equals(contentScheme)) {
            mime = context.getContentResolver().getType(uri);
        } else if (uri.getScheme().equals(fileScheme)) {
            FileNameMap fileNameMap = URLConnection.getFileNameMap();
            mime = fileNameMap.getContentTypeFor(uri.getLastPathSegment());
        }
        return mime;
    }

    @SuppressLint("Range")
    public String resolveFilename(Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            cursor.close();
            return name;
        }
        return null;
    }

    private void sendFileWithNewTempInternal(Uri uri) {
        String mime = resolveMimeType(uri);
        String name = resolveFilename(uri);
        String extension = mime == null ? null : MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
        File file = null;
        try (InputStream inp = context.getContentResolver().openInputStream(uri)) {
            if (inp != null) {
                file = File.createTempFile(TAG, extension, context.getCacheDir());
                writeFully(file, inp);
            }
        } catch (IOException e) {
            Log.e(TAG, "failed to copy selected file", e);
            if (file != null) {
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    Log.w(TAG, "failed to deleted file " + file.getName());
                }
                file = null;
            }
        }

        if (file != null && name != null && mime != null) {
            File finalFile = file;

            MessageStream.SendFileCallback callback = new MessageStream.SendFileCallback() {
                @Override
                public void onProgress(@NonNull Message.Id id, long sentBytes) {
                }

                @Override
                public void onSuccess(@NonNull Message.Id id) {
                    finalFile.delete();
                }

                @Override
                public void onFailure(@NonNull Message.Id id, @NonNull WebimError<SendFileError> error) {
                    finalFile.delete();
                    handleFileError(error);
                }
            };
            mainHandler.post(() -> session.getStream().sendFile(finalFile, name, mime, callback));
        } else {
            mainHandler.post(() -> showErrorTost(context.getString(R.string.file_upload_failed_unknown)));
        }
    }

    private static void writeFully(@NonNull File to, @NonNull InputStream from) throws IOException {
        byte[] buffer = new byte[4096];

        try (OutputStream out = new FileOutputStream(to)) {
            for (int read; (read = from.read(buffer)) != -1; ) {
                out.write(buffer, 0, read);
            }
        }
    }

    private void handleFileError(@NonNull WebimError<MessageStream.SendFileCallback.SendFileError> error) {
        if (context != null) {
            String message;
            switch (error.getErrorType()) {
                case FILE_TYPE_NOT_ALLOWED:
                    message = context.getString(R.string.file_upload_failed_type);
                    break;
                case FILE_SIZE_EXCEEDED:
                    message = context.getString(R.string.file_upload_failed_size);
                    break;
                case FILE_NAME_INCORRECT:
                    message = context.getString(
                        R.string.file_upload_failed_name);
                    break;
                case UNAUTHORIZED:
                    message = context.getString(R.string.file_upload_failed_unauthorized);
                    break;
                case FILE_IS_EMPTY:
                    message = context.getString(R.string.file_upload_failed_empty);
                    break;
                case UPLOADED_FILE_NOT_FOUND:
                default:
                    message = context.getString(R.string.file_upload_failed_unknown);
            }
            showErrorTost(message);
        }
    }

    private void showErrorTost(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}