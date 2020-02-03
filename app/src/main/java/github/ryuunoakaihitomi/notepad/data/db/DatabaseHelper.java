package github.ryuunoakaihitomi.notepad.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DB_NAME = "notes.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        setWriteAheadLoggingEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String createSql = "CREATE TABLE " + DatabaseConstants.TABLE_NAME + " ("
                + DatabaseConstants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DatabaseConstants.COLUMN_NAME_TITLE + " TEXT,"
                + DatabaseConstants.COLUMN_NAME_BODY + " TEXT,"
                + DatabaseConstants.COLUMN_NAME_UPDATE_TIME + " INTEGER"
                + ")";
        db.execSQL(createSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "onUpgrade: drop table. ver( " + oldVersion + " -> " + newVersion + " )");
        final String upgradeSql = "DROP TABLE IF EXISTS " + DatabaseConstants.TABLE_NAME;
        db.execSQL(upgradeSql);
        onCreate(db);
    }
}
