package github.ryuunoakaihitomi.notepad.ui;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import github.ryuunoakaihitomi.notepad.util.UiUtils;

public class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "MyALC";

    private Activity mActivity;

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        preventWindowLeaked(activity);
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPreStopped(@NonNull Activity activity) {
        Log.i(TAG, "onActivityPreStopped " + activity.getLocalClassName());
        preventWindowLeaked(activity);  //在29中保证取消dialog优先?
    }

    private void preventWindowLeaked(Activity activity) {
        if (Objects.equals(mActivity, activity)) {
            Dialog dialog = UiUtils.getDialog();
            if (dialog != null && dialog.isShowing()) {
                Log.i(TAG, "preventWindowLeaked: cancel dialog");
                dialog.cancel();
            }
            mActivity = null;
        }
    }
}
