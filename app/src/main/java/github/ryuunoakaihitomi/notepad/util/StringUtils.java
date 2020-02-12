package github.ryuunoakaihitomi.notepad.util;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StringUtils {

    private StringUtils() {
    }

    public static boolean isEmptyAfterTrim(String s) {
        return TextUtils.isEmpty(s) || s.trim().length() == 0;
    }

    public static String trimAll(String src) {
        return src.replaceAll("\\s+", "");
    }

    public static String trimToValidFileName(String fileName) {
        return fileName.replaceAll("[/\\\\:*?\"<>|]", "");
    }

    public static String inputStreamToString(InputStream stream) {
        String string = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();
            while ((string = reader.readLine()) != null)
                builder.append(string).append(System.lineSeparator());
            return builder.toString();
        } catch (IOException ignored) {
        }
        return string;
    }
}
