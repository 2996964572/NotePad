package github.ryuunoakaihitomi.notepad.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Arrays;

import github.ryuunoakaihitomi.notepad.util.hook.ReflectionUtils;

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
            Log.d(TAG, "onWindowFocusChanged: focus lost in marquee. [text,hashCode]: " +
                    Arrays.asList(getText(), hashCode()));
        }
        super.onWindowFocusChanged(true);
    }

    private boolean canMarquee() {
        return (boolean) ReflectionUtils.invokeMethod(
                ReflectionUtils.findMethod(
                        TextView.class, "canMarquee"), this);
    }
}
