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

-dontwarn sun.misc.**

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {*;}

-keepclassmembers class rx.internal.util.unsafe.ConcurrentCircularArrayQueue {*;}

-keepclassmembers class rx.internal.util.unsafe.ConcurrentSequencedCircularArrayQueue {*;}

-keepclassmembers class rx.internal.util.unsafe.MpmcArrayQueueConsumerField {*;}

-keepclassmembers class rx.internal.util.unsafe.MpscLinkedQueue {*;}

-keepclassmembers class rx.internal.util.unsafe.SpmcArrayQueueConsumerField {*;}

-keepclassmembers class rx.internal.util.unsafe.SpscArrayQueue {*;}

-keepclassmembers class rx.internal.util.unsafe.SpscUnboundedArrayQueue {*;}

-keepclassmembers class rx.internal.util.unsafe.UnsafeAccess {*;}

