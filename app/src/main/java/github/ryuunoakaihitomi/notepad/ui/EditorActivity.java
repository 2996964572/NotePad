package github.ryuunoakaihitomi.notepad.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import github.ryuunoakaihitomi.notepad.BuildConfig;
import github.ryuunoakaihitomi.notepad.R;
import github.ryuunoakaihitomi.notepad.data.bean.Note;
import github.ryuunoakaihitomi.notepad.transaction.MainTransaction;
import github.ryuunoakaihitomi.notepad.util.ContentUtils;
import github.ryuunoakaihitomi.notepad.util.InternalRes;
import github.ryuunoakaihitomi.notepad.util.StringUtils;
import github.ryuunoakaihitomi.notepad.util.TimeUtils;
import github.ryuunoakaihitomi.notepad.util.UiUtils;
import github.ryuunoakaihitomi.notepad.widget.EditorEditText;

public class EditorActivity extends Activity implements LoaderManager.LoaderCallbacks<List<Note>> {

    private static final String SHORTCUT_ACTION_CREATE = BuildConfig.APPLICATION_ID + ".CREATE_NOTE_FROM_SHORTCUT";
    public static final int LOADER_ID = 33;
    private static final String TAG = "EditorActivity";
    private static final String
            EXTRA_TAG_TYPE = "extra.type",
            EXTRA_TAG_ID = "extra.id",
            EXTRA_TAG_CONTENT = "extra.content",
            BUNDLE_TAG_TITLE = "bundle.title",
            BUNDLE_TAG_BODY = "bundle.body";
    private static final int TITLE_MAX_LENGTH = 140;
    private ActionType mType;
    private long mId;
    private EditText mTitleEditor;
    private EditorEditText mBodyEditor;
    private String[] mExtraContent;
    private boolean mTextChanged, mCalledFromShortcut, mEditorLocked, mUpdateInitWorkExecuted, mPrepareToExit;

    public static void actionStart(Context context, @NonNull ActionType type, long id, String[] content) {
        Intent intent = new Intent(context, EditorActivity.class);
        intent.putExtra(EXTRA_TAG_TYPE, type);
        intent.putExtra(EXTRA_TAG_ID, id);
        intent.putExtra(EXTRA_TAG_CONTENT, content);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mTitleEditor = findViewById(R.id.et_editor_title_editor);
        mBodyEditor = findViewById(R.id.et_editor_body_editor);

        mTitleEditor.getPaint().setFakeBoldText(true);
        mTitleEditor.setFilters(new InputFilter[]{new InputFilter.LengthFilter(TITLE_MAX_LENGTH)});

        Intent intent = getIntent();
        String action = intent.getAction();
        Log.d(TAG, "onCreate: action=" + action);

        boolean isFromSend = Intent.ACTION_SEND.equals(action) && "text/plain".equals(intent.getType());
        if (!isFromSend) {
            mType = (ActionType) intent.getSerializableExtra(EXTRA_TAG_TYPE);
            if (mType == null && SHORTCUT_ACTION_CREATE.equals(intent.getAction())) {
                Log.i(TAG, "onCreate: create note from shortcut");
                mType = ActionType.CREATE;
                mCalledFromShortcut = true;
            }
            mId = intent.getLongExtra(EXTRA_TAG_ID, 0);
            mExtraContent = intent.getStringArrayExtra(EXTRA_TAG_CONTENT);
        } else mType = ActionType.SEND;

        if (mType.equals(ActionType.UPDATE) && mId != 0) {
            Log.i(TAG, "onCreate: change note " + mId);
            getLoaderManager().initLoader(LOADER_ID, MainTransaction.getArgBundle(MainTransaction.ActionType.FIND_BY_ID, mId, null, null), this);
        } else if (mType.equals(ActionType.CREATE) && mExtraContent != null && mExtraContent.length == 2) {
            Log.i(TAG, "onCreate: create note from extra");
            mTitleEditor.setText(mExtraContent[0]);
            mBodyEditor.setText(mExtraContent[1]);
        } else if (isFromSend) {
            Log.i(TAG, "onCreate: create note from send");
            mTitleEditor.setText(intent.getStringExtra(Intent.EXTRA_SUBJECT));
            mBodyEditor.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
        }

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mType.equals(ActionType.UPDATE)) {
                    if (mUpdateInitWorkExecuted) {
                        Log.v(TAG, "onTextChanged: update init work executed");
                        mTextChanged = true;
                    }
                } else mTextChanged = true;

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        mTitleEditor.addTextChangedListener(watcher);
        mBodyEditor.addTextChangedListener(watcher);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        save();
        if (!mType.equals(ActionType.SEND) && !mCalledFromShortcut) MainActivity.start(this);
        mPrepareToExit = true;
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        if (((PowerManager) Objects.requireNonNull(getSystemService(POWER_SERVICE))).isScreenOn() &&
                !mPrepareToExit) {
            Log.e(TAG, "onStop: Exit without onBackPressed()");
            if (mTextChanged) UiUtils.showToast(this, R.string.note_discard_hint);
            finish();
        }
        super.onStop();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        UiUtils.setOptionalIconsVisibleOnMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor, menu);

        MenuItem countItem = menu.findItem(R.id.item_editor_count);
        countItem.setIcon(InternalRes.getDrawable(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ?
                InternalRes.R.drawable.ic_menu_find_holo_dark : InternalRes.R.drawable.ic_menu_find_mtrl_alpha));
        MenuItem copyItem = menu.findItem(R.id.item_editor_copy);
        copyItem.setIcon(InternalRes.getDrawable(InternalRes.R.drawable.ic_menu_copy_holo_dark));
        MenuItem lockItem = menu.findItem(R.id.item_editor_edit_lock);
        lockItem.setTitle(R.string.lock);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable lockIcon = InternalRes.getDrawable(InternalRes.R.drawable.ic_lock_outline_wht_24dp);
            lockIcon.setTint(Color.RED);
            lockItem.setIcon(lockIcon);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_editor_discard:
                MainActivity.start(this);
                UiUtils.showToast(this, R.string.note_discard_hint);
                finish();
                break;
            case R.id.item_editor_clear:
                mTitleEditor.setText(null);
                mBodyEditor.setText(null);
                break;
            case R.id.item_editor_copy:
                ContentUtils.copyToClipboard(this, String.format(getString(R.string.content_copy_template), mTitleEditor.getText(), mBodyEditor.getText()));
                UiUtils.showToast(this, InternalRes.getString(InternalRes.R.string.text_copied));
                break;
            case R.id.item_editor_count:
                String title = mTitleEditor.getText().toString(), body = mBodyEditor.getText().toString();
                UiUtils.showMessageDialog(this, null, String.format(
                        Locale.getDefault(),
                        getString(R.string.count_words_content),
                        title.length(),
                        StringUtils.trimAll(title).length(),
                        body.length(),
                        StringUtils.trimAll(body).length()));
                break;
            case R.id.item_editor_edit_lock:
                item.setTitle(mEditorLocked ? R.string.lock : R.string.unlock);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Drawable drawable = InternalRes.getDrawable(mEditorLocked ?
                            InternalRes.R.drawable.ic_lock_outline_wht_24dp :
                            InternalRes.R.drawable.ic_lock_open_wht_24dp);
                    drawable.setTint(mEditorLocked ? Color.RED : Color.GREEN);
                    item.setIcon(drawable);
                }
                UiUtils.setEditTextEditable(mTitleEditor, mEditorLocked);
                UiUtils.setEditTextEditable(mBodyEditor, mEditorLocked);
                mEditorLocked = !mEditorLocked;
                break;
            case R.id.item_editor_force_save:
                save();
                mTextChanged = false;
                break;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_TAG_TITLE, mTitleEditor.getText().toString());
        outState.putString(BUNDLE_TAG_BODY, mBodyEditor.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        mTitleEditor.setText(savedInstanceState.getString(BUNDLE_TAG_TITLE));
        mBodyEditor.setText(savedInstanceState.getString(BUNDLE_TAG_BODY));
    }

    private void save() {
        if (!mType.equals(ActionType.SEND) && !mTextChanged && mExtraContent == null) return;
        UiUtils.curtain(false, mTitleEditor, mBodyEditor);
        String title = mTitleEditor.getText().toString();
        String body = mBodyEditor.getText().toString();
        if (StringUtils.isEmptyAfterTrim(title) && StringUtils.isEmptyAfterTrim(body)) {
            Log.e(TAG, "onBackPressed: title and body are empty");
            Toast.makeText(this, R.string.content_null, Toast.LENGTH_SHORT).show();
            return;
        }
        String nowTime = TimeUtils.getLocalFormatText(System.currentTimeMillis());
        if (StringUtils.isEmptyAfterTrim(title)) {
            Log.w(TAG, "onBackPressed: title is empty");
            mTitleEditor.setText(nowTime);
        } else if (StringUtils.isEmptyAfterTrim(body)) {
            Log.w(TAG, "onBackPressed: body is empty");
            mTitleEditor.setText(nowTime);
            mBodyEditor.setText(title);
        }
        switch (mType) {
            case CREATE:
            case SEND:
                getLoaderManager().initLoader(LOADER_ID, MainTransaction.getArgBundle(MainTransaction.ActionType.INSERT, 0, fetchNoteFromEditor(), null), this);
                break;
            case UPDATE:
                getLoaderManager().restartLoader(LOADER_ID, MainTransaction.getArgBundle(MainTransaction.ActionType.UPDATE, mId, fetchNoteFromEditor(), null), this);
                break;
        }
        UiUtils.showToast(this, R.string.note_saved, true);
    }

    private Note fetchNoteFromEditor() {
        Note note = new Note();
        note.setTitle(mTitleEditor.getText().toString());
        note.setBody(mBodyEditor.getText().toString());
        note.setUpdateTime(System.currentTimeMillis());
        return note;
    }

    @Override
    public Loader<List<Note>> onCreateLoader(int id, Bundle args) {
        return new MainTransaction(this, args);
    }

    @Override
    public void onLoadFinished(Loader<List<Note>> loader, List<Note> data) {
        Note note = data.get(0);
        switch (mType) {
            case CREATE:
            case SEND:
                Log.i(TAG, "onLoadFinished: insert -> id:" + note.getId());
                break;
            case UPDATE:
                Log.i(TAG, "onLoadFinished: update -> id:" + note.getId());
                if (!mUpdateInitWorkExecuted) {
                    mTitleEditor.setText(note.getTitle());
                    mBodyEditor.setText(note.getBody());
                    Log.d(TAG, "onLoadFinished: update data loaded");
                    mUpdateInitWorkExecuted = true;
                }
                break;
        }
        UiUtils.curtain(true, mTitleEditor, mBodyEditor);
    }

    @Override
    public void onLoaderReset(Loader<List<Note>> loader) {
        Log.d(TAG, "onLoaderReset");
    }

    public enum ActionType {
        CREATE, UPDATE, SEND
    }
}
