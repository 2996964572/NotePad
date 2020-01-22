package github.ryuunoakaihitomi.notepad.util.hook;

import android.os.Build;
import android.util.Log;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("WeakerAccess")
public class ReflectionUtils {

    private static final String TAG = "ReflectionUtils";

    static {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            Log.w(TAG, "static initializer: need clear classLoader");
            HookUtils.clearClassLoader(ReflectionUtils.class);
        }
    }

    private ReflectionUtils() {
    }

    private static Class<?> findClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Method findMethod(String clazzName, String methodName, Class<?>... paramTypes) {
        return findMethod(findClass(clazzName), methodName, paramTypes);
    }

    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        if (clazz == null) {
            Log.e(TAG, "findMethod: clazz is null");
            return null;
        }
        Method method;
        try {
            method = clazz.getDeclaredMethod(name, paramTypes);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getMethod(name, paramTypes);
            } catch (NoSuchMethodException ex) {
                return null;
            }
        }
        setAccessible(method);
        return method;
    }

    public static Object invokeMethodNullInstance(Method method, Object... args) {
        return invokeMethod(method, null, args);
    }

    public static Object invokeMethod(Method method, Object instance, Object... args) {
        if (method == null) {
            Log.e(TAG, "invokeMethod: method is null");
            return null;
        }
        try {
            return method.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
        throw new IllegalStateException();
    }

    public static Field findField(String clazzName, String fieldName) {
        return findField(findClass(clazzName), fieldName);
    }

    public static Field findField(Class<?> clazz, String name) {
        if (clazz == null) {
            Log.e(TAG, "findField: clazz is null");
            return null;
        }
        Field field;
        try {
            field = clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            try {
                field = clazz.getField(name);
            } catch (NoSuchFieldException ex) {
                return null;
            }
        }
        setAccessible(field);
        return field;
    }

    public static Object fetchField(Field field) {
        return fetchField(field, null);
    }

    public static Object fetchField(Field field, Object instance) {
        if (field == null) {
            Log.e(TAG, "fetchField: field is null");
            return null;
        }
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public static Method findMethodCallerSensitive(Class<?> clazz, String name, Class<?>... paramTypes) {
        Class<?> clsClz = Class.class;
        Method
                getDeclaredMethod = findMethod(clsClz, "getDeclaredMethod", String.class, Class[].class),
                getMethod = findMethod(clsClz, "getMethod", String.class, Class[].class),
                method;
        if (getDeclaredMethod == null || getMethod == null) {
            Log.e(TAG, "findMethodCallerSensitive: Cannon reflect java.lang.reflect.*");
            return null;
        }
        method = (Method) invokeMethod(getDeclaredMethod, clazz, name, paramTypes);
        if (method == null) {
            method = (Method) invokeMethod(getMethod, clazz, name, paramTypes);
        }
        if (method != null) {
            setAccessible(method);
        }
        return method;
    }

    public static Method findMethodCallerSensitive(String clazzName, String methodName, Class<?>... paramTypes) {
        return findMethodCallerSensitive(findClass(clazzName), methodName, paramTypes);
    }

    private static void setAccessible(AccessibleObject accessibleObject) {
        if (!accessibleObject.isAccessible()) {
            Log.i(TAG, "setAccessible: [" + accessibleObject + "] is not accessible");
            accessibleObject.setAccessible(true);
        }
    }
}
