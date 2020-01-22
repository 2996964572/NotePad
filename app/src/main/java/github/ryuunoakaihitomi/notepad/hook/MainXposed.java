package github.ryuunoakaihitomi.notepad.hook;

import android.util.Log;

import androidx.annotation.Keep;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.ryuunoakaihitomi.notepad.BuildConfig;

@Keep
public class MainXposed implements IXposedHookLoadPackage {

    private static final String TAG = "MainXposed";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (BuildConfig.APPLICATION_ID.equals(lpparam.packageName)) {
            Log.i(TAG, "handleLoadPackage: start to hook.");
            new SelfHook().handleLoadPackage(lpparam);
        }
    }
}
