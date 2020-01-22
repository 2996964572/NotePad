package github.ryuunoakaihitomi.notepad.data.dao;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import github.ryuunoakaihitomi.notepad.data.NoteProvider;
import github.ryuunoakaihitomi.notepad.data.bean.Note;
import github.ryuunoakaihitomi.notepad.data.db.DatabaseConstants;


public class NoteDao implements Dao<Note>, BaseColumns {

    private static final String BASE_URI = NoteProvider.SCHEME + NoteProvider.CONTENT_AUTHORITY + "/";
    private static final String DIR_URI = BASE_URI + DatabaseConstants.TABLE_NAME;
    private static final String ITEM_URI = DIR_URI + "/#";


    @SuppressLint("StaticFieldLeak")    // 传入AppContext
    private static NoteDao sSingleton;
    private final Context mContext;

    private NoteDao(Context context) {
        mContext = context;
    }

    public static NoteDao getInstance(Context context) {
        if (sSingleton == null) {
            synchronized (NoteDao.class) {
                if (sSingleton == null) {
                    sSingleton = new NoteDao(context.getApplicationContext());
                }
            }
        }
        return sSingleton;
    }

    @Override
    public long insert(Note entity) {
        Uri uri = mContext.getContentResolver().insert(Uri.parse(ITEM_URI), getContentValuesFromNote(entity));
        if (uri != null) return ContentUris.parseId(uri);
        return 0;
    }

    @Override
    public List<Note> findAll() {
        Cursor cursor = mContext.getContentResolver().query(Uri.parse(DIR_URI), null, null, null, null);
        assert cursor != null;
        return getNoteListFromCursor(cursor);
    }

    @Override
    public Note findById(long id) {
        Cursor cursor = mContext.getContentResolver().query(ContentUris.withAppendedId(Uri.parse(ITEM_URI), id), null, null, null, null);
        assert cursor != null;
        return getNoteListFromCursor(cursor).get(0);
    }

    @Override
    public void update(long id, Note entity) {
        mContext.getContentResolver().update(ContentUris.withAppendedId(Uri.parse(ITEM_URI), id), getContentValuesFromNote(entity), null, null);
    }

    @Override
    public void delete(long id) {
        mContext.getContentResolver().delete(ContentUris.withAppendedId(Uri.parse(ITEM_URI), id), null, null);
    }

    /**
     * 根据标题查找记事（模糊查询）
     *
     * @param title 标题
     * @return 记事列表
     */
    public List<Note> findByTitle(String title) {
        return getNoteListFromCursor(fuzzyQuery(mContext.getContentResolver(), DatabaseConstants.COLUMN_NAME_TITLE, title));
    }

    /**
     * 根据标题查找记事（模糊查询）
     *
     * @param body 内容
     * @return 记事列表
     */
    public List<Note> findByBody(String body) {
        return getNoteListFromCursor(fuzzyQuery(mContext.getContentResolver(), DatabaseConstants.COLUMN_NAME_BODY, body));
    }

    private ContentValues getContentValuesFromNote(Note note) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseConstants.COLUMN_NAME_TITLE, note.getTitle());
        cv.put(DatabaseConstants.COLUMN_NAME_BODY, note.getBody());
        cv.put(DatabaseConstants.COLUMN_NAME_UPDATE_TIME, note.getUpdateTime());
        return cv;
    }

    private List<Note> getNoteListFromCursor(Cursor cursor) {
        List<Note> notes = new ArrayList<>();
        int titleColIdx = cursor.getColumnIndex(DatabaseConstants.COLUMN_NAME_TITLE),
                bodyColIdx = cursor.getColumnIndex(DatabaseConstants.COLUMN_NAME_BODY),
                idColIdx = cursor.getColumnIndex(_ID),
                updateTimeColIdx = cursor.getColumnIndex(DatabaseConstants.COLUMN_NAME_UPDATE_TIME);
        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setTitle(cursor.getString(titleColIdx));
                note.setBody(cursor.getString(bodyColIdx));
                note.setId(cursor.getLong(idColIdx));
                note.setUpdateTime(cursor.getLong(updateTimeColIdx));
                notes.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notes;
    }

    /* 模糊（包含）查询 */
    private Cursor fuzzyQuery(ContentResolver cr, String columnName, String key) {
        String customUri = BASE_URI + NoteProvider.TABLE_TAG_FOR_CUSTOM;
        return cr.query(Uri.parse(customUri), null, columnName + " LIKE ?", new String[]{"%" + key + "%"}, null);
    }
}
