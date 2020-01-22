package github.ryuunoakaihitomi.notepad.util;

import android.text.TextUtils;

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
}
