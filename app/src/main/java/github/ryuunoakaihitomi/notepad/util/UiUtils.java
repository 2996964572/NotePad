package github.ryuunoakaihitomi.notepad.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.UiThread;

import java.lang.reflect.Proxy;

import github.ryuunoakaihitomi.notepad.util.hack.ReflectionUtils;

@UiThread
public class UiUtils {

    private static final String TAG = "UiUtils";

    private static AlertDialog sDialog;

    private UiUtils() {
    }

    public static void showToast(@NonNull Context context, @StringRes int msg, boolean isLong) {
        showToast(context, context.getString(msg), isLong);
    }

    public static void showToast(@NonNull Context context, String msg, boolean isLong) {
        boolean isMi = AndroidCompat.isMiui();
        Toast toast = Toast.makeText(context.getApplicationContext(), isMi ? "" : msg, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        if (isMi) toast.setText(msg);
        toast.show();
    }

    public static void showToast(@NonNull Context context, String msg) {
        showToast(context, msg, false);
    }

    public static void showToast(@NonNull Context context, @StringRes int msg) {
        showToast(context, msg, false);
    }

    public static void setOptionalIconsVisibleOnMenu(Menu menu) {
        ReflectionUtils.invokeMethod(
                ReflectionUtils.findMethod(menu.getClass(), "setOptionalIconsVisible", Boolean.TYPE), menu, true);
    }

    public static void showMessageDialog(Context context, @StringRes int title, @StringRes int msg) {
        showMessageDialog(context, context.getString(title), context.getString(msg));
    }

    public static void showMessageDialog(Context context, @StringRes int title, @StringRes int msg, @ColorInt int msgColor) {
        showMessageDialog(context, title, msg);
        setAlertDialogMessageTextColor(sDialog, msgColor);
    }

    public static void showMessageDialog(Context context, String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        sDialog = builder.show();
    }

    public static void setWindowTransparency(
            @NonNull Window w,
            @FloatRange(from = 0, to = 1, fromInclusive = false, toInclusive = false) float alpha,
            @FloatRange(from = 0, to = 1) float backgroundDarkness) {
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.alpha = alpha;
        // 只在show()之后生效
        lp.dimAmount = backgroundDarkness;
        w.setAttributes(lp);
    }

    /**
     * 在{@link android.app.Activity}中设置{@link AlertDialog}时必须设置以防止{@code android.view.WindowLeaked}
     *
     * @param dialog sDialog
     * @see github.ryuunoakaihitomi.notepad.ui.MyActivityLifecycleCallbacks
     */
    public static void setDialog(@NonNull AlertDialog dialog) {
        Log.d(TAG, "setDialog: " + dialog);
        sDialog = dialog;
    }

    @Nullable
    public static AlertDialog getDialog() {
        return sDialog;
    }

    public static void hideCloseButtonOnSearchView(SearchView view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            ImageView mCloseButtonInstance;
            mCloseButtonInstance = (ImageView) ReflectionUtils.fetchField(
                    ReflectionUtils.findField(SearchView.class, "mCloseButton"), view);
            if (mCloseButtonInstance != null) {
                mCloseButtonInstance.setEnabled(false);
                mCloseButtonInstance.setImageDrawable(null);
            }
        }
    }

    @SafeVarargs
    public static <V extends View> void curtain(boolean visibility, @NonNull V... view) {
        for (V v : view) v.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
    }

    @SuppressLint({"DiscouragedPrivateApi", "JavaReflectionMemberAccess", "PrivateApi"})
    public static void defineSystemToast() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Log.d(TAG, "defineSystemToast");
            Object notificationService = ReflectionUtils.invokeMethodNullInstance(
                    ReflectionUtils.findMethod(Toast.class, "getService"));
            ReflectionUtils.setFieldNullInstance(ReflectionUtils.findField(Toast.class, "sService"),
                    Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                            new Class[]{ReflectionUtils.findClass("android.app.INotificationManager")}, (proxy, method, args) -> {
                                if ("enqueueToast".equals(method.getName())) {
                                    Log.d(TAG, "enqueueToast: " + "Duration:" + args[2]);
                                    args[0] = "android";
                                }
                                return method.invoke(notificationService, args);
                            }));
        }
    }

    public static void setEditTextEditable(EditText editText, boolean editable) {
        if (!editable) {
            InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(editText.getApplicationWindowToken(), 0);
        }
        editText.setFocusable(editable);
        editText.setFocusableInTouchMode(editable);
        editText.setLongClickable(editable);
    }

    public static void setAlertDialogMessageTextColor(AlertDialog dialog, @ColorInt int color) {
        Object alertController = ReflectionUtils.fetchField(ReflectionUtils.findField(AlertDialog.class, "mAlert"), dialog);
        if (alertController != null) {
            TextView msgView = (TextView) ReflectionUtils.fetchField(ReflectionUtils.findField(alertController.getClass(), "mMessageView"), alertController);
            if (msgView != null) {
                Log.i(TAG, "setAlertDialogMessageTextColor: " + Integer.toHexString(color));
                msgView.setTextColor(color);
            }
        }
    }
}
