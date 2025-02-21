package ru.webim.android.demo.util;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ru.webim.android.sdk.WebimLog;

public class FileLogger implements WebimLog {
    private final File file;
    private final Handler handler;

    private static FileLogger instance;

    private FileLogger(File file) {
        this.file = file;
        HandlerThread handlerThread = new HandlerThread(this.getClass().getSimpleName());
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public synchronized void log(String message) {
        handler.post(() -> {
            try(FileWriter fileWriter = new FileWriter(file, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

                bufferedWriter.append(message);
                bufferedWriter.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Nullable
    public synchronized static FileLogger getAppSpecificDirLogger(Context context, String filename)  {
        if (instance == null) {
            if (!checkExternalStorageAvailable()) {
                return null;
            }
            File directory = context.getExternalFilesDir(null);
            File file = new File(directory, filename);

            instance = new FileLogger(file);
        }
        return instance;
    }

    private static boolean checkExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
