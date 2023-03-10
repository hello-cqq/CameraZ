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
-repackageclasses com.ahahahq.barcode
-keep class com.google.zxing.** { *; }
-keep class com.ahahahq.barcode.Decoder {*;}
-keep class com.ahahahq.barcode.qrcode.QRCodeUtil {*;}
-keep class com.ahahahq.barcode.qrcode.* {*;}
-keep class com.ahahahq.barcode.qrcode.ErrorLevel {*;}
-keep class com.ahahahq.barcode.base.CodeType {*;}
-keep class com.ahahahq.barcode.base.Result {*;}
-keep class com.ahahahq.barcode.base.BarcodeOptions {*;}
-keep class com.ahahahq.barcode.base.BarcodeOptions$* {*;}

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