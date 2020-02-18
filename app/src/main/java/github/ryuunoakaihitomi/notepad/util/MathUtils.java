package github.ryuunoakaihitomi.notepad.util;

import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils {

    private static final String TAG = "MathUtils";

    private MathUtils() {
    }

    public static int getDigitLength(long num) {
        if (num == 0) return 1;
        num = Math.abs(num);
        return (int) Math.log10(num) + 1;
    }

    public static long average(long... numberList) {
        long len = numberList.length, sum = 0;
        for (long num : numberList) sum += num;
        return sum / len;
    }

    static float percentage(long part, long total) {
        if (part > total) {
            Log.w(TAG, "percentage: part > total, swap the params.");
            part ^= total;
            total ^= part;
            part ^= total;
        }
        BigDecimal ratio = BigDecimal.valueOf((float) part / total * Math.pow(10, 2));
        return ratio.setScale(2, RoundingMode.HALF_UP).floatValue();
    }
}
