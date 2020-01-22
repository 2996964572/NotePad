package github.ryuunoakaihitomi.notepad.hook;

import android.app.AlertDialog;
import android.graphics.Color;
import android.util.Log;

import java.util.Random;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.ryuunoakaihitomi.notepad.BuildConfig;
import github.ryuunoakaihitomi.notepad.util.UiUtils;

class SelfHook implements IXposedHookLoadPackage {

    private static final String TAG = "SelfHook";

    private XC_LoadPackage.LoadPackageParam mLpparam;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        mLpparam = lpparam;

        signalModuleActive();
        colorDialogMsgText();
        printAllClasses();
    }

    private void signalModuleActive() {
        Class<?> constantClazz = XposedHelpers.findClass(BuildConfig.APPLICATION_ID + ".hook.XposedConstants", mLpparam.classLoader);
        XposedHelpers.findAndHookMethod(constantClazz, "isModuleActive", XC_MethodReplacement.returnConstant(true));
    }

    private void colorDialogMsgText() {
        XposedHelpers.findAndHookMethod(AlertDialog.Builder.class, "show", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                AlertDialog dialog = (AlertDialog) param.getResult();
                int color = Color.BLACK + new Random().nextInt(Color.WHITE - Color.BLACK);
                Log.d(TAG, "afterHookedMethod: colorDialogMsgText() random color is " + Integer.toHexString(color));
                UiUtils.setAlertDialogMessageTextColor(dialog, color);
            }
        });
    }

    private void printAllClasses() {
        XposedHelpers.findAndHookMethod(ClassLoader.class, "loadClass", String.class, new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (param.hasThrowable()) return;
                Class<?> clazz = (Class<?>) param.getResult();
                final String clazzName = clazz.getName();
                Log.d(TAG, "printAllClasses: loadClass " + clazzName);
            }
        });
    }
}
