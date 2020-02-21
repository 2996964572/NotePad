package github.ryuunoakaihitomi.notepad.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewDebug;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;

import github.ryuunoakaihitomi.notepad.BuildConfig;
import github.ryuunoakaihitomi.notepad.util.hack.ReflectionUtils;

public class ListTitleTextView extends TextView {

    private static final String TAG = "ListTitleTextView";

    public ListTitleTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setTextAppearance(context, android.R.style.TextAppearance_Large);
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setSingleLine();

        // 保持marquee效果
        setSelected(true);

    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        /* 在弹出上下文菜单时仍保持marquee效果 */
        if (!hasWindowFocus && canMarquee()) {
            Log.d(TAG, "onWindowFocusChanged: focus lost in marquee. hashes = " + Arrays.asList(hashCode(), Objects.hashCode(getText())));
            if (BuildConfig.DEBUG) {
                ViewDebug.dumpCapturedView(TAG, this);

                // doc from View superclass
                /*
                 * Prints information about this view in the log output, with the tag
                 * {@link #VIEW_LOG_TAG}. Each line in the output is preceded with an
                 * indentation defined by the <code>depth</code>.
                 *
                 * @param depth the indentation level
                 *
                 * @hide
                 */
                debug(0);
            }
        }
        super.onWindowFocusChanged(true);
    }

    private boolean canMarquee() {
        return (boolean) ReflectionUtils.invokeMethod(
                ReflectionUtils.findMethod(
                        TextView.class, "canMarquee"), this);
    }
}
