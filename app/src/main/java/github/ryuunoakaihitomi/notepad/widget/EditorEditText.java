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
import android.view.ViewDebug;
import android.widget.EditText;

import androidx.annotation.RequiresApi;

import github.ryuunoakaihitomi.notepad.BuildConfig;
import github.ryuunoakaihitomi.notepad.util.FileUtils;
import github.ryuunoakaihitomi.notepad.util.MathUtils;
import github.ryuunoakaihitomi.notepad.util.OsUtils;
import github.ryuunoakaihitomi.notepad.util.hack.ReflectionUtils;

public class EditorEditText extends EditText implements View.OnFocusChangeListener {

    private static final String TAG = "EditorEditText";

    private final Rect mRect;
    private final Paint mPaint;
    private boolean mHasFocus, mPowerSaveMode;

    public EditorEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        // 去除默认的下划线
        setBackground(null);

        setOnFocusChangeListener(this);

        // 在省电模式下禁用一部分视觉效果
        mPowerSaveMode = OsUtils.isPowerSaverEnabled(getContext());
        if (mPowerSaveMode) {
            Log.i(TAG, "constructor: Power save mode is enabled. Disable some visual effects.");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "mPaint.getNativeInstance() " + getPaintNativeInstance());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mPowerSaveMode) {
            super.onDraw(canvas);
            return;
        }

        Rect r = mRect;
        Paint paint = mPaint;
        Layout layout = getLayout();

        paint.setColor(Color.GRAY);
        paint.setTextSize(getLineHeight() >> 1);
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
        mPowerSaveMode = OsUtils.isPowerSaverEnabled(getContext());

        if (BuildConfig.DEBUG) debug(0);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @ViewDebug.CapturedViewProperty // method must be public
    public long getPaintNativeInstance() {
        return (long) ReflectionUtils.invokeMethod(ReflectionUtils.findMethod(Paint.class, "getNativeInstance"), mPaint);
    }

    @Override
    public void debug(int depth) {
        ViewDebug.dumpCapturedView(TAG, this);
        super.debug(depth);
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility != View.VISIBLE &&
                !mPowerSaveMode &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "setVisibility: invisible... size of content is " + FileUtils.getSizeString(getText().toString().getBytes().length));
        }
        super.setVisibility(visibility);
    }
}
