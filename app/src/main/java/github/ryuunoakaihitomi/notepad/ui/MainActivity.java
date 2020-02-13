package github.ryuunoakaihitomi.notepad.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import github.ryuunoakaihitomi.notepad.R;
import github.ryuunoakaihitomi.notepad.adapter.NoteAdapter;
import github.ryuunoakaihitomi.notepad.data.bean.Note;
import github.ryuunoakaihitomi.notepad.hook.XposedConstants;
import github.ryuunoakaihitomi.notepad.transaction.AsyncLoader;
import github.ryuunoakaihitomi.notepad.util.ContentUtils;
import github.ryuunoakaihitomi.notepad.util.FileUtils;
import github.ryuunoakaihitomi.notepad.util.Global;
import github.ryuunoakaihitomi.notepad.util.InternalRes;
import github.ryuunoakaihitomi.notepad.util.OsUtils;
import github.ryuunoakaihitomi.notepad.util.StringUtils;
import github.ryuunoakaihitomi.notepad.util.TimeUtils;
import github.ryuunoakaihitomi.notepad.util.UiUtils;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnLongClickListener, LoaderManager.LoaderCallbacks<List<Note>> {

    public static final int LOADER_ID = 22;
    private static final String TAG = "MainActivity";
    private NoteAdapter mNoteAdapter;
    private int mSelectedItemPosition;
    private ListView mListView;
    private boolean mExportToken;

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = findViewById(R.id.list_main_note_list);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        TextView emptyView = findViewById(R.id.tv_main_empty_view);
        mListView.setEmptyView(emptyView);
        emptyView.setOnLongClickListener(this);

        registerForContextMenu(mListView);

        UiUtils.createAppShortcut(MainActivity.this, R.string.create_note, android.R.drawable.ic_menu_add,
                new Intent(EditorActivity.SHORTCUT_ACTION_CREATE));

        Bundle args = AsyncLoader.getArgBundle(AsyncLoader.ActionType.FIND_ALL, 0, null, null);
        getLoaderManager().initLoader(LOADER_ID, args, this);

        if (XposedConstants.isModuleActive()) {
            UiUtils.showMessageDialog(this, R.string.xposed_warning_title, R.string.xposed_description, Color.RED);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.item_main_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Bundle bundle = AsyncLoader.getArgBundle(AsyncLoader.ActionType.SEARCH, 0, null, newText);
                getLoaderManager().restartLoader(LOADER_ID, bundle, MainActivity.this);
                return false;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable materialSearchIcon = InternalRes.getDrawable(InternalRes.R.drawable.ic_menu_search_mtrl_alpha);
            materialSearchIcon.setTint(Color.CYAN);
            searchItem.setIcon(materialSearchIcon);
        }
        UiUtils.hideCloseButtonOnSearchView(searchView);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        mSelectedItemPosition = info.position;
        new MenuInflater(this).inflate(R.menu.list, menu);

        MenuItem shareItem = menu.findItem(R.id.item_list_share_note);
        shareItem.setTitle(InternalRes.getString(InternalRes.R.string.share));
        MenuItem deleteItem = menu.findItem(R.id.item_list_delete_note);
        deleteItem.setTitle(InternalRes.getString(InternalRes.R.string.delete));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_main_create_note:
                EditorActivity.actionStart(this, EditorActivity.ActionType.CREATE, 0, null);
                finish();
                break;
            case R.id.item_main_help:
                String helpContent = StringUtils.inputStreamToString(getResources().openRawResource(R.raw.help));
                if (TextUtils.isEmpty(helpContent)) {
                    Log.e(TAG, "onOptionsItemSelected: help content for " + OsUtils.getLanguage() + " is not implemented!");
                    break;
                }
                WebView webView = null;
                try {
                    webView = new WebView(getApplicationContext());
                    WebSettings webSettings = webView.getSettings();
                    webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
                    webSettings.setLoadWithOverviewMode(true);
                    webSettings.setSupportZoom(false);

                    String mimeType = Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1 ?
                            "text/html; charset=" + StandardCharsets.UTF_8.name() :
                            null;

                    webView.loadData(helpContent, mimeType, null);
                } catch (Throwable t) {
                    //android.util.AndroidRuntimeException: android.webkit.WebViewFactory$MissingWebViewPackageException: Failed to load WebView provider: No WebView installed
                    Log.e(TAG, "onOptionsItemSelected: WebView", t);
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle(R.string.help);
                if (webView != null) builder.setView(webView);
                else {
                    Log.w(TAG, "onOptionsItemSelected: setMessage(html) to show help");
                    builder.setMessage(Html.fromHtml(helpContent));
                }
                UiUtils.setDialog(builder.show());
                break;
            case R.id.item_main_about:
                UiUtils.showMessageDialog(this, R.string.about, R.string.about_content);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int positionForShow = mSelectedItemPosition + 1;
        final Note note = mNoteAdapter.getItem(mSelectedItemPosition);
        assert note != null;
        Bundle bundle;
        switch (item.getItemId()) {
            case R.id.item_list_share_note:
                ContentUtils.share(this, note.getTitle(), note.getBody());
                break;
            case R.id.item_list_copy_note:
                note.setUpdateTime(System.currentTimeMillis());
                bundle = AsyncLoader.getArgBundle(AsyncLoader.ActionType.INSERT, 0, note, null);
                getLoaderManager().restartLoader(LOADER_ID, bundle, this);
                UiUtils.showToast(this, String.format(Locale.getDefault(), getString(R.string.copy_note_hint), positionForShow));
                break;
            case R.id.item_list_export_note:
                bundle = AsyncLoader.getArgBundle(AsyncLoader.ActionType.EXPORT, 0, note, null);
                mExportToken = true;
                getLoaderManager().restartLoader(LOADER_ID, bundle, this);
                break;
            case R.id.item_list_delete_note:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                AlertDialog delDialog = builder.setTitle(R.string.delete_confirm)
                        .setMessage(String.format(Locale.getDefault(), getString(R.string.delete_confirm_content), positionForShow))
                        .setCancelable(false)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.ok, (dialog, which) ->
                                getLoaderManager().restartLoader(LOADER_ID, AsyncLoader.getArgBundle(AsyncLoader.ActionType.DELETE,
                                        note.getId(), null, null), MainActivity.this)).show();
                UiUtils.setDialog(delDialog);
                break;
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick: pos=" + position + " id=" + id);
        EditorActivity.actionStart(this, EditorActivity.ActionType.UPDATE, id, null);
        finish();
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        UiUtils.setOptionalIconsVisibleOnMenu(menu);
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemLongClick: pos=" + position + " id=" + id);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || parent.getFirstVisiblePosition() > 0)
            UiUtils.showToast(this, String.format(getString(R.string.ctx_menu_hint), position + 1), true);
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.tv_main_empty_view) {
            AlertDialog debugDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.debug_dialog_title)
                    .setMessage(R.string.debug_dialog_message)
                    .setPositiveButton(R.string.test_crash, (dialog, which) -> {
                        throw new RuntimeException("Test Crash");
                    })
                    .setNegativeButton(R.string.logcat, (dialog, which) -> {
                        String name = "env_" + TimeUtils.getNowId();
                        UiUtils.showToast(getApplicationContext(), name, true);
                        new Thread(() -> {
                            String path = getExternalFilesDir(Global.LOG_DIR_NAME) + File.separator + name + ".txt";
                            OsUtils.logcatToFile(path);
                        }).start();
                    })
                    .setNeutralButton(R.string.export_database, (dialog, which) -> {
                        String name = Global.EXPORT_DATABASE_DIR_NAME + "_" + TimeUtils.getNowId();
                        UiUtils.showToast(MainActivity.this, name, true);
                        new Thread(() -> {
                            String path = getExternalFilesDir(Global.EXPORT_DATABASE_DIR_NAME) + File.separator + name + ".zip";
                            String src = getDatabasePath("placeholder").getParent();
                            FileUtils.compress(src, path);
                        }).start();
                    })
                    .show();
            debugDialog.getButton(Dialog.BUTTON_NEGATIVE).setAllCaps(false);
            UiUtils.setDialog(debugDialog);
            UiUtils.setAlertDialogMessageTextColor(debugDialog, Color.YELLOW);
        }
        return true;
    }

    @Override
    public Loader<List<Note>> onCreateLoader(int id, Bundle args) {
        return new AsyncLoader(this, args);
    }

    @Override
    public void onLoadFinished(Loader<List<Note>> loader, List<Note> data) {
        if (mNoteAdapter == null) {
            mNoteAdapter = new NoteAdapter(this, R.layout.item_note, data);
            mListView.setAdapter(mNoteAdapter);
        } else if (mExportToken) {
            UiUtils.showMessageDialog(this, getString(R.string.file_saved_to), data.get(0).getBody());
            mExportToken = false;
        } else {
            /* 避开导出返回路径数据 */
            if (data.size() == 1 && data.get(0).getTitle() == null) {
                Log.d(TAG, "onLoadFinished: data from export file path. don't reload");
                return;
            }
            mNoteAdapter.reload(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Note>> loader) {
    }
}
