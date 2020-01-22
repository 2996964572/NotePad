-optimizationpasses 99
# 清除日志代码
-assumenosideeffects class android.util.Log {
    public static int *(...);
}
# 保留行号并且隐藏类名
-renamesourcefileattribute sf
-keepattributes sf,LineNumberTable

-repackageclasses ''
-allowaccessmodification
-verbose
