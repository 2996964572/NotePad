package github.ryuunoakaihitomi.notepad.util.hack;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;

public class HookUtils {

    private static final String TAG = "HookUtils";

    private HookUtils() {
    }

    public static void removeReflectRestriction(Context context) {
        switch (Build.VERSION.SDK_INT) {
            case Build.VERSION_CODES.Q:
                String vmRuntimeClassName = "dalvik.system.VMRuntime";
                Object runtime = ReflectionUtils.invokeMethodNullInstance(ReflectionUtils.findMethod(vmRuntimeClassName, "getRuntime"));
                ReflectionUtils.invokeMethod(ReflectionUtils.findMethodCallerSensitive(vmRuntimeClassName, "setHiddenApiExemptions", String[].class),
                        runtime,
                        (Object) new String[]{"L"});
                break;
            case Build.VERSION_CODES.P:
                ApplicationInfo applicationInfo = context.getApplicationInfo();
                ReflectionUtils.invokeMethod(
                        ReflectionUtils.findMethodCallerSensitive(ApplicationInfo.class, "setHiddenApiEnforcementPolicy", int.class),
                        applicationInfo, 0);
                break;
            default:
                Log.w(TAG, "removeReflectRestriction: unnecessary");
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static void clearClassLoader(Class cls) {
        String unsafeClassName = "sun.misc.Unsafe";
        Object unsafeInstance = ReflectionUtils.fetchField(
                ReflectionUtils.findField(unsafeClassName, "theUnsafe"));
        long offset = (long) ReflectionUtils.invokeMethod(
                ReflectionUtils.findMethod(
                        unsafeClassName, "objectFieldOffset", Field.class),
                unsafeInstance, ReflectionUtils.findField(
                        Class.class, "classLoader"));
        Log.d(TAG, "clearClassLoader: offset=" + offset);
        ReflectionUtils.invokeMethod(
                ReflectionUtils.findMethod(
                        unsafeClassName, "putObject", Object.class, long.class, Object.class),
                unsafeInstance, cls, offset, null);
    }
}
