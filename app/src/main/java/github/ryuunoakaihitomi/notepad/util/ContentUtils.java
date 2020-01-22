package github.ryuunoakaihitomi.notepad.util;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Objects;

public class ContentUtils {

    private static final String TAG = "ContentUtils";

    private ContentUtils() {
    }

    public static void share(@NonNull Context context, String subject, String text) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(Intent.createChooser(sendIntent, subject)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "share: ActivityNotFoundException");
        }
    }

    public static void copyToClipboard(Context context, String text) {
        ((ClipboardManager) Objects.requireNonNull(context.getSystemService(Context.CLIPBOARD_SERVICE)))
                .setPrimaryClip(ClipData.newPlainText(null, text));
    }
}
