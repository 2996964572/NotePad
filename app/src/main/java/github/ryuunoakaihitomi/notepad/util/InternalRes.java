package github.ryuunoakaihitomi.notepad.util;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;

import java.util.Arrays;

public class InternalRes {

    private static final String TAG = "InternalRes";

    private static final Resources sSysRes = Resources.getSystem();

    private InternalRes() {
    }

    @AnyRes
    private static int getAndroidResId(String resType, String resName) {
        int id = sSysRes.getIdentifier(resName, resType, "android");
        if (id == 0)
            Log.e(TAG, "getAndroidResId: " + Arrays.asList(resType, resName) + " not found on " + Build.VERSION.SDK_INT);
        return id;
    }

    public static String getString(@NonNull String resName) {
        return sSysRes.getString(getAndroidResId("string", resName));
    }

    public static Drawable getDrawable(@NonNull String resName) {
        return sSysRes.getDrawable(getAndroidResId("drawable", resName));
    }


    public final class R {

        public final class string {
            public static final String share = "share";
            public static final String delete = "delete";
            public static final String text_copied = "text_copied";
            public static final String storage_internal = "storage_internal";
        }

        @SuppressWarnings("SpellCheckingInspection")
        public final class drawable {
            public static final String ic_menu_search_mtrl_alpha = "ic_menu_search_mtrl_alpha";
            public static final String ic_menu_find_holo_dark = "ic_menu_find_holo_dark";
            public static final String ic_menu_find_mtrl_alpha = "ic_menu_find_mtrl_alpha";
            public static final String ic_menu_copy_holo_dark = "ic_menu_copy_holo_dark";
            public static final String ic_lock_outline_wht_24dp = "ic_lock_outline_wht_24dp";
            public static final String ic_lock_open_wht_24dp = "ic_lock_open_wht_24dp";
        }
    }
}
