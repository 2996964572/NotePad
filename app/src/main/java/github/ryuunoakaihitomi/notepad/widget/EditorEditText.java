package github.ryuunoakaihitomi.notepad.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.RequiresApi;

import java.lang.reflect.Method;

import github.ryuunoakaihitomi.notepad.util.MathUtils;
import github.ryuunoakaihitomi.notepad.util.hack.ReflectionUtils;

public class EditorEditText extends EditText implements View.OnFocusChangeListener {

    private static final String TAG = "EditorEditText";

    private final Rect mRect;
    private final Paint mPaint;
    private boolean mHasFocus;

    public EditorEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        // 去除默认的下划线
        setBackground(null);

        setOnFocusChangeListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "mPaint.getNativeInstance() " + getPaintNativeInstance());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Rect r = mRect;
        Paint paint = mPaint;
        Layout layout = getLayout();

        paint.setColor(Color.GRAY);
        paint.setTextSize(getLineHeight() / 2);
        int count = getLineCount();
        for (int i = 0; i < count; i++) {
            int baseline = getLineBounds(i, r);
            int showLineNum = i + 1;
            int lineTextLength = layout.getLineEnd(i) - layout.getLineEnd(i - 1) - (i == count - 1 ? 0 : 1);

            // 绘画行线
            canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            // 绘画居中行号
            canvas.drawText(String.valueOf(showLineNum),
                    MathUtils.average(r.left, r.right) - paint.getTextSize() * MathUtils.getDigitLength(showLineNum) / 4,
                    baseline,
                    paint);
            // 绘画每行字数于行尾
            if (lineTextLength > 0) {
                canvas.drawText(String.valueOf(lineTextLength),
                        r.right - paint.getTextSize() * (float) (MathUtils.getDigitLength(lineTextLength) / 2.0 + 1),
                        baseline,
                        paint);
            }
        }

        /* 绘画边框 */
        if (mHasFocus) {
            paint.setColor(Color.RED);
            if (getLocalVisibleRect(r)) {
                r.inset(1, 1);
                canvas.drawRect(r, paint);
            }
        }

        super.onDraw(canvas);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        mHasFocus = hasFocus;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private long getPaintNativeInstance() {
        Method method = ReflectionUtils.findMethod(Paint.class, "getNativeInstance");
        if (method != null) {
            return (long) ReflectionUtils.invokeMethod(method, mPaint);
        }
        return -1;
    }
}
