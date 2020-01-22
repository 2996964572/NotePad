package github.ryuunoakaihitomi.notepad.transaction;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.WorkerThread;

import java.io.File;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import github.ryuunoakaihitomi.notepad.BuildConfig;
import github.ryuunoakaihitomi.notepad.R;
import github.ryuunoakaihitomi.notepad.data.bean.Note;
import github.ryuunoakaihitomi.notepad.data.dao.NoteDao;
import github.ryuunoakaihitomi.notepad.ui.EditorActivity;
import github.ryuunoakaihitomi.notepad.ui.MainActivity;
import github.ryuunoakaihitomi.notepad.util.FileUtils;
import github.ryuunoakaihitomi.notepad.util.InternalRes;
import github.ryuunoakaihitomi.notepad.util.StringUtils;
import github.ryuunoakaihitomi.notepad.util.TimeUtils;

public class AsyncLoader extends AsyncTaskLoader<List<Note>> {

    private static final String TAG = "AsyncLoader";

    static {
        Logger.getLogger(TAG).severe("CRUD");
        LoaderManager.enableDebugLogging(BuildConfig.DEBUG);
    }

    private List<Note> mNoteList = new ArrayList<>();
    private Bundle mArgs;

    public AsyncLoader(Context context, Bundle args) {
        super(context);
        mArgs = args;
    }

    public static Bundle getArgBundle(@ActionTypeDef int action, long id, Note note, String keyword) {
        Bundle bundle = new Bundle();
        bundle.putInt(ArgKey.ACTION, action);
        bundle.putLong(ArgKey.ID, id);
        if (note != null) bundle.putParcelable(ArgKey.NOTE, note);
        if (keyword != null) bundle.putString(ArgKey.KEYWORD, keyword);
        return bundle;
    }

    @WorkerThread
    @Override
    public List<Note> loadInBackground() {
        long start = SystemClock.currentThreadTimeMillis();
        int action = mArgs.getInt(ArgKey.ACTION);
        Log.i(TAG, "loadInBackground: action=" + action + " thread=" + Thread.currentThread());
        long id = mArgs.getLong(ArgKey.ID);
        Note note = mArgs.getParcelable(ArgKey.NOTE);
        NoteDao noteDao = NoteDao.getInstance(getContext());
        switch (action) {
            case ActionType.FIND_ALL:
                mNoteList = noteDao.findAll();
                break;
            case ActionType.FIND_BY_ID:
                mNoteList = Collections.singletonList(noteDao.findById(id));
                break;
            case ActionType.SEARCH:
                /* 当搜索标题的结果不存在时，再搜索内容 */
                String keyWord = mArgs.getString(ArgKey.KEYWORD);
                if (TextUtils.isEmpty(keyWord)) {
                    Log.v(TAG, "loadInBackground: keyword is empty");
                    mNoteList = noteDao.findAll();
                } else {
                    mNoteList = noteDao.findByTitle(keyWord);
                    if (mNoteList.size() == 0) {
                        Log.w(TAG, "loadInBackground: findByTitle(keyWord) is empty");
                        mNoteList = noteDao.findByBody(keyWord);
                    }
                }
                break;
            case ActionType.INSERT:
                long insertRow = noteDao.insert(note);
                if (getId() == MainActivity.LOADER_ID) mNoteList = noteDao.findAll();
                else {
                    if (note != null) note.setId(insertRow);
                    Log.v(TAG, "loadInBackground: return note to EditorActivity. From " + action);
                    return Collections.singletonList(note);
                }
                Log.d(TAG, "loadInBackground: insertRow=" + insertRow);
                break;
            case ActionType.UPDATE:
                noteDao.update(id, note);
                if (getId() == EditorActivity.LOADER_ID) {
                    Log.v(TAG, "loadInBackground: return note to EditorActivity. From " + action);
                    return Collections.singletonList(note);
                }
                break;
            case ActionType.DELETE:
                noteDao.delete(id);
                mNoteList = noteDao.findAll();
                break;
            case ActionType.EXPORT:
                assert note != null;
                String title = note.getTitle(), body = note.getBody();
                String fullPath = Objects.requireNonNull(
                        getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)).getAbsolutePath() + File.separator +
                        StringUtils.trimToValidFileName(title + "." + TimeUtils.getNowId() + ".txt");
                FileUtils.writeTextFile(fullPath,
                        String.format(getContext().getString(R.string.content_copy_template), title, body));
                Note savePathNote = new Note();
                // 将路径通过记事内容返回
                savePathNote.setBody(fullPath.replace(Environment.getExternalStoragePublicDirectory("").getAbsolutePath(),
                        InternalRes.getString(InternalRes.R.string.storage_internal)));
                return Collections.singletonList(savePathNote);
            default:
                Log.e(TAG, "loadInBackground: !");
        }
        Log.i(TAG, "loadInBackground: duration=" + (SystemClock.currentThreadTimeMillis() - start));
        return mNoteList;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public void deliverResult(List<Note> data) {
        super.deliverResult(data);
    }

    private interface ArgKey {
        String
                ACTION = "action",
                ID = "id",
                NOTE = "note",
                KEYWORD = "keyword";
    }

    public interface ActionType {
        int
                FIND_ALL = 1,
                FIND_BY_ID = 2,
                SEARCH = 3,
                INSERT = 4,
                UPDATE = 5,
                DELETE = 6,
                EXPORT = 7;
    }

    @Documented
    @IntDef({
            ActionType.FIND_ALL,
            ActionType.DELETE,
            ActionType.FIND_BY_ID,
            ActionType.INSERT,
            ActionType.SEARCH,
            ActionType.UPDATE,
            ActionType.EXPORT
    })
    @Retention(RetentionPolicy.SOURCE)
    private @interface ActionTypeDef {
    }
}
