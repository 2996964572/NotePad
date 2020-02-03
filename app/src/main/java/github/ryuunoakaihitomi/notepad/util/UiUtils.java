package github.ryuunoakaihitomi.notepad.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.UiThread;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;

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
        Toast toast = Toast.makeText(context.getApplicationContext(), isMi ? null : msg, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
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
        if (menu != null) {
            Method setOptionalIconsVisible = ReflectionUtils.findMethod(menu.getClass(), "setOptionalIconsVisible", Boolean.TYPE);
            if (setOptionalIconsVisible != null) {
                ReflectionUtils.invokeMethod(setOptionalIconsVisible, menu, true);
            }
        }
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
            Field mCloseButton = ReflectionUtils.findField(SearchView.class, "mCloseButton");
            ImageView mCloseButtonInstance = null;
            if (mCloseButton != null) {
                mCloseButtonInstance = (ImageView) ReflectionUtils.fetchField(mCloseButton, view);
            }
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

    @SuppressWarnings("JavaReflectionMemberAccess")
    public static void defineSystemToast() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Log.d(TAG, "defineSystemToast");
            try {
                @SuppressLint("DiscouragedPrivateApi")
                Method getServiceMethod = Toast.class.getDeclaredMethod("getService");
                getServiceMethod.setAccessible(true);
                final Object iNotificationManagerObj = getServiceMethod.invoke(null);
                @SuppressLint("PrivateApi")
                Class iNotificationManagerCls = Class.forName("android.app.INotificationManager");
                Object iNotificationManagerProxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                        new Class[]{iNotificationManagerCls}, (proxy, method, args) -> {
                            if ("enqueueToast".equals(method.getName())) {
                                Log.d(TAG, "methodName:" + method.getName() + " D:" + args[2]);
                                args[0] = "android";
                            }
                            return method.invoke(iNotificationManagerObj, args);
                        });
                Field sServiceField = Toast.class.getDeclaredField("sService");
                sServiceField.setAccessible(true);
                sServiceField.set(null, iNotificationManagerProxy);
            } catch (Exception e) {
                Log.e(TAG, "defineSystemToast: ", e);
            }
        }
    }

    public static void createAppShortcut(Context context, @StringRes int title, @DrawableRes int icon, Intent i) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            String titleStr = context.getString(title);
            ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(context, titleStr)
                    .setShortLabel(titleStr)
                    .setIcon(Icon.createWithResource(context, icon))
                    .setIntent(i)
                    .build();
            ShortcutManager manager = context.getSystemService(ShortcutManager.class);
            if (manager != null) {
                manager.setDynamicShortcuts(Collections.singletonList(shortcutInfo));
            }
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
