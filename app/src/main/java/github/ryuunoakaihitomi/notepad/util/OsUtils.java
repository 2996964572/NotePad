package github.ryuunoakaihitomi.notepad.util;

import android.app.ActivityManager;
import android.app.ApplicationErrorReport;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Process;
import android.util.ArrayMap;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import github.ryuunoakaihitomi.notepad.util.hook.ReflectionUtils;

public class OsUtils {

    private static final String TAG = "OsUtils";

    private OsUtils() {
    }

    public static String getJsonBuildInfo() {
        try {
            Class buildClass = Build.class;
            Map<String, Object> map = new ArrayMap<>();
            for (Field f : buildClass.getFields()) {
                map.put(f.getName(), ReflectionUtils.fetchField(f));
            }
            Class buildVersionClass = Build.VERSION.class;
            for (Field f : buildVersionClass.getFields()) {
                map.put("VERSION." + f.getName(), ReflectionUtils.fetchField(f));
            }
            Map<String, Object> sort = new TreeMap<>(String::compareTo);
            sort.putAll(map);
            JSONObject buildJson = new JSONObject(sort);
            return buildJson.toString(2);
        } catch (JSONException e) {
            Log.e(TAG, "getJsonBuildInfo: ", e);
            return null;
        }
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

    public static String getJsonCrashReportInfo(String packageName, String processName, long time, boolean systemApp, String installerPackageName, Throwable t) {
        try {
            ApplicationErrorReport report = new ApplicationErrorReport();
            report.type = ApplicationErrorReport.TYPE_CRASH;
            report.packageName = packageName;
            report.processName = processName;
            report.time = time;
            report.systemApp = systemApp;
            report.installerPackageName = installerPackageName;
            report.crashInfo = new ApplicationErrorReport.CrashInfo(t);
            Class aerClass = ApplicationErrorReport.class;
            Map<String, Object> map = new TreeMap<>();
            for (Field f : aerClass.getFields()) {
                Object obj = ReflectionUtils.fetchField(f, report);
                if (obj instanceof ApplicationErrorReport.CrashInfo)
                    for (Field _f : obj.getClass().getFields())
                        map.put("CrashInfo." + _f.getName(), ReflectionUtils.fetchField(_f, obj));
                map.put(f.getName(), obj);
            }
            JSONObject crashJson = new JSONObject(map);
            return crashJson.toString(2);
        } catch (JSONException e) {
            Log.e(TAG, "getJsonBuildInfo: ", e);
            return null;
        }
    }
}
