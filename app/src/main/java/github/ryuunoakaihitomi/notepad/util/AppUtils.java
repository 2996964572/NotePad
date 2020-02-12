package github.ryuunoakaihitomi.notepad.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Process;

import java.util.List;

public class AppUtils {

    private AppUtils() {
    }

    public static String getCurrentProcessName(Context context) {
        int pid = Process.myPid();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        assert am != null;
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) return null;
        for (ActivityManager.RunningAppProcessInfo info : runningApps)
            if (info.pid == pid) return info.processName;
        return null;
    }

    public static boolean isSystemApp(Context context) {
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) > 0;
    }
}
