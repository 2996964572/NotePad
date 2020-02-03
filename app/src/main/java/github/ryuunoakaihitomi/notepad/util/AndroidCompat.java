package github.ryuunoakaihitomi.notepad.util;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

import github.ryuunoakaihitomi.notepad.util.hack.ReflectionUtils;

public class AndroidCompat {

    private static final String TAG = "AndroidCompat";

    private AndroidCompat() {
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static boolean isMiui() {
        //android.os.SystemProperties.get("ro.miui.ui.version.name", "");
        String miuiVersionName;
        Method SystemProperties = ReflectionUtils.findMethod("android.os.SystemProperties", "get", String.class, String.class);
        if (SystemProperties != null) {
            miuiVersionName = String.valueOf(ReflectionUtils.invokeMethodNullInstance(SystemProperties, "ro.miui.ui.version.name", ""));
        } else return false;
        Log.d(TAG, "isMiui: miuiVersionName:" + miuiVersionName);
        return !TextUtils.isEmpty(miuiVersionName);
    }
}
