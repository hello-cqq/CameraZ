# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-ignorewarnings
-repackageclasses com.ahahahq.cameraz
-keep class com.ahahahq.cameraz.callback.* {*;}
-keep class com.ahahahq.cameraz.core.CameraZ {*;}
-keep class com.ahahahq.cameraz.core.CameraZ$Companion {*;}
-keep class com.ahahahq.cameraz.core.CameraClient {*;}
-keep class com.ahahahq.cameraz.core.CameraClient$CameraState {*;}
-keep class com.ahahahq.cameraz.core.CameraException {*;}
-keep class com.ahahahq.cameraz.executor.* {*;}
-keep class com.ahahahq.cameraz.model.* {*;}
-keep class com.ahahahq.cameraz.ui.* {*;}
-keep class com.ahahahq.cameraz.util.* {*;}
-keep class com.ahahahq.cameraz.common.* {*;}
-keep public class com.ahahahq.cameraz.view.CameraView
-keep public class * extends android.view.View{
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    *** get*();
    void set*(***);
}
-keepclassmembers class ** {
    public <init>(***);
}
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers class **.R$* {
    public static <fields>;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(***);
}
-keep class * implements android.os.Parcelable { *; }
-keep class * implements java.io.Serializable { *; }