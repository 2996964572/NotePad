package github.ryuunoakaihitomi.notepad.hook;

import androidx.annotation.Keep;

public class XposedConstants {

    private XposedConstants() {
    }

    @SuppressWarnings("SameReturnValue")
    @Keep
    public static boolean isModuleActive() {
        return false;
    }
}
