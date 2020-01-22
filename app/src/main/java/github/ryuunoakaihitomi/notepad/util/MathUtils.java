package github.ryuunoakaihitomi.notepad.util;

public class MathUtils {

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
}
