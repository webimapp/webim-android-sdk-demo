package ru.webim.android.sdk.impl;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Handler;
import androidx.annotation.NonNull;

import com.google.gson.Gson;
import ru.webim.android.sdk.FAQ;
import ru.webim.android.sdk.FAQCategory;
import ru.webim.android.sdk.FAQItem;
import ru.webim.android.sdk.FAQStructure;
import ru.webim.android.sdk.impl.items.FAQCategoryItem;
import ru.webim.android.sdk.impl.items.FAQItemItem;
import ru.webim.android.sdk.impl.items.FAQStructureItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FAQSQLiteHistoryStorage {
    private static final Executor executor = new ThreadPoolExecutor(0, 1, 60,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    private static final String INSERT_HISTORY_STATEMENT = "INSERT OR FAIL INTO name" +
            "(id, " +
            "data) " +
            "VALUES (?,?)";
    private static final String UPDATE_HISTORY_STATEMENT = "UPDATE name " +
            "SET " +
            "data=? " +
            "WHERE id=?";
    private static final int VERSION = 2;

    private final FAQSQLiteHistoryStorage.MyDBHelper dbHelper;
    private final Handler handler;

    public FAQSQLiteHistoryStorage(Context context,
                                   Handler handler,
                                   String dbName) {
        this.dbHelper = new FAQSQLiteHistoryStorage.MyDBHelper(context, dbName);
        this.handler = handler;
    }

    public void insertCategory(final String id, final FAQCategoryItem category) {
        String data = new Gson().toJson(category);
        insert(id, data, "categories");
    }

    public void insertStructure(final String id, final FAQStructureItem structure) {
        String data = new Gson().toJson(structure);
        insert(id, data, "structures");
    }

    public void insertItem(final String id, final FAQItemItem item) {
        String data = new Gson().toJson(item);
        insert(id, data, "items");
    }

    private void insert(final String id, final String data, final String tableName) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                SQLiteStatement insertStatement
                        = db.compileStatement(INSERT_HISTORY_STATEMENT.replace("name", tableName));
                SQLiteStatement updateStatement
                        = db.compileStatement(UPDATE_HISTORY_STATEMENT.replace("name", tableName));
                try {
                    insertStatement.bindString(1, id);
                    insertStatement.bindString(2, data);
                    insertStatement.executeInsert();

                } catch (SQLiteConstraintException ignored) {
                    updateStatement.bindString(1, id);
                    updateStatement.bindString(2, data);
                    updateStatement.executeUpdateDelete();
                } catch (SQLException ignored) {
                } finally {
                    insertStatement.clearBindings();
                    updateStatement.clearBindings();
                }

                insertStatement.close();
                updateStatement.close();

                db.close();
            }
        });
    }

    public void getCategory(final String id,
                            @NonNull final FAQ.GetCallback<FAQCategory> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor cursor = db.rawQuery("SELECT data FROM categories WHERE id = ? LIMIT 1",
                        new String[] { id });
                List<FAQCategory> list = new ArrayList<>();
                try {
                    while (cursor.moveToNext()) {
                        list.add(toCategory(cursor));
                    }
                } finally {
                    cursor.close();
                    db.close();
                }

                runCallback(list, callback);
            }
        });
    }

    public void getStructure(final String id,
                            @NonNull final FAQ.GetCallback<FAQStructure> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor cursor = db.rawQuery("SELECT data FROM structures WHERE id = ? LIMIT 1",
                    new String[] { id });
                List<FAQStructure> list = new ArrayList<>();
                try {
                    while (cursor.moveToNext()) {
                        list.add(toStructure(cursor));
                    }
                } finally {
                    cursor.close();
                    db.close();
                }

                runCallback(list, callback);
            }
        });
    }

    public void getItem(final String id,
                            @NonNull final FAQ.GetCallback<FAQItem> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor cursor = db.rawQuery("SELECT data FROM items WHERE id = ? LIMIT 1",
                    new String[]{ id });

                List<FAQItem> list = new ArrayList<>();
                try {
                    while (cursor.moveToNext()) {
                        list.add(toItem(cursor));
                    }
                } finally {
                    cursor.close();
                    db.close();
                }

                runCallback(list, callback);
            }
        });
    }

    private <T> void runCallback(List<T> list, FAQ.GetCallback<T> callback) {
        if (!list.isEmpty()) {
            runSuccessCallback(callback, list.get(0));
        } else {
            runErrorCallback(callback);
        }
    }

    private <T> void runSuccessCallback(final FAQ.GetCallback<T> callback, final T object) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.receive(object);
            }
        });
    }

    private <T> void runErrorCallback(final FAQ.GetCallback<T> callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onError();
            }
        });
    }


    private FAQCategory toCategory(Cursor cursor) {
        String data = cursor.getString(0);
        return new Gson().fromJson(data, FAQCategoryItem.class);
    }

    private FAQStructure toStructure(Cursor cursor) {
        String data = cursor.getString(0);
        return new Gson().fromJson(data, FAQStructureItem.class);
    }

    private FAQItem toItem(Cursor cursor) {
        String data = cursor.getString(0);
        return new Gson().fromJson(data, FAQItemItem.class);
    }

    private static class MyDBHelper extends SQLiteOpenHelper {

        private static final String CREATE_TABLE = "CREATE TABLE categories\n"
                + "(\n"
                + "    id INT PRIMARY KEY NOT NULL,\n"
                + "    data TEXT\n"
                + ")";

        public MyDBHelper(Context context, String dbName) {
            super(context, dbName, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String[] tableNames = { "categories", "structures", "items" };
            for (String name : tableNames) {
                db.execSQL("CREATE TABLE " + name + "\n"
                        + "(\n"
                        + "    id INT PRIMARY KEY NOT NULL,\n"
                        + "    data TEXT\n"
                        + ")");
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion != newVersion) {
                db.execSQL("DROP TABLE history");
                onCreate(db);
            }
        }
    }
}
