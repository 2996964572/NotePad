package github.ryuunoakaihitomi.notepad.util;

import android.app.ApplicationErrorReport;
import android.os.Build;
import android.system.Os;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.WorkerThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import github.ryuunoakaihitomi.notepad.util.hack.ReflectionUtils;

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
                    for (Field _f : obj.getClass().getFields()) {
                        Object val = ReflectionUtils.fetchField(_f, obj);
                        /* CrashInfo.stackTrace */
                        if (val instanceof String) {
                            String valStr = (String) val;
                            if (valStr.contains(System.lineSeparator()))
                                val = valStr.replace("\t", "    ").split(System.lineSeparator());
                        }
                        map.put("CrashInfo." + _f.getName(), val);
                    }
                map.put(f.getName(), obj);
            }
            JSONObject crashJson = new JSONObject(map);
            return crashJson.toString(2);
        } catch (JSONException e) {
            Log.e(TAG, "getJsonBuildInfo: ", e);
            return null;
        }
    }

    @WorkerThread
    public static void logcatToFile(String path) {
        Log.d(TAG, "logcatToFile: start");
        try {
            // -d              Dump the log and then exit (don't block)
            // -f <file>, --file=<file>               Log to file. Default is stdout
            List<String> cmdList = new ArrayList<>(Arrays.asList("logcat", "-d", "-f", path));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                //  --pid=<pid>     Only prints logs from the given pid.
                cmdList.add("--pid=" + Os.getpid());
            String[] command = new String[cmdList.size()];
            cmdList.toArray(command);
            Process exec = Runtime.getRuntime().exec(command);
            int status = exec.waitFor();
            Log.d(TAG, "logcatToFile: status=" + status);
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "logcatToFile: ", e);
        }
    }

    public static String getLanguage() {
        Locale locale = Locale.getDefault();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return locale.toLanguageTag();
        } else {
            return locale.toString();
        }
    }
}
