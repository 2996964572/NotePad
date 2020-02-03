package github.ryuunoakaihitomi.notepad.data.db;

import android.provider.BaseColumns;

public class DatabaseConstants implements BaseColumns {

    private DatabaseConstants() {
    }

    public static final String TABLE_NAME = "note";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_BODY = "body";
    public static final String COLUMN_NAME_UPDATE_TIME = "updateTime";
}
