package github.ryuunoakaihitomi.notepad.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Loader;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

import github.ryuunoakaihitomi.notepad.BuildConfig;
import github.ryuunoakaihitomi.notepad.R;
import github.ryuunoakaihitomi.notepad.data.bean.Note;
import github.ryuunoakaihitomi.notepad.transaction.MainTransaction;
import github.ryuunoakaihitomi.notepad.util.TimeUtils;
import github.ryuunoakaihitomi.notepad.util.UiUtils;

@TargetApi(Build.VERSION_CODES.M)
public class QuickRecordActivity extends Activity implements LoaderManager.LoaderCallbacks<List<Note>> {

    public static final int LOADER_ID = 5;
    private static final String TAG = "QuickRecordActivity";
    private String mTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (!Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
            Log.e(TAG, "onCreate: unexpected intent!");
            finish();
            return;
        }

        String callingPkg = getCallingPackage();
        ComponentName callingAct = getCallingActivity();
        Log.d(TAG, "onCreate: calling info = " + Arrays.asList(callingPkg, callingAct));
        if (callingPkg == null /* Calling from help dialog's WebView. */ || callingPkg.equals(BuildConfig.APPLICATION_ID)) {
            Log.e(TAG, "onCreate: called by myself!");
            UiUtils.showToast(this, R.string.quick_record_internal_warning);
            finish();
            return;
        }

        CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        boolean isReadOnly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true);

        if (TextUtils.isEmpty(text)) {
            Log.e(TAG, "onCreate: text from extra of Intent.EXTRA_PROCESS_TEXT is empty!");
            finish();
            return;
        }

        Note note = new Note();
        long now = System.currentTimeMillis();
        mTitle = TimeUtils.getLocalFormatText(now);
        note.setUpdateTime(now);
        note.setTitle(mTitle);
        note.setBody(text.toString());

        Log.d(TAG, "onCreate: now saving... rec info = " + Arrays.asList(text, now, isReadOnly));
        getLoaderManager().initLoader(LOADER_ID, MainTransaction.getArgBundle(MainTransaction.ActionType.INSERT, 0, note, null), this);
    }

    @Override
    public Loader<List<Note>> onCreateLoader(int id, Bundle args) {
        return new MainTransaction(this, args);
    }

    @Override
    public void onLoadFinished(Loader<List<Note>> loader, List<Note> data) {
        Log.d(TAG, "onLoadFinished: saved!");
        UiUtils.showToast(this, String.format(getString(R.string.quick_record_saved_hint), mTitle), true);
        finish();
    }

    @Override
    public void onLoaderReset(Loader<List<Note>> loader) {
    }
}
