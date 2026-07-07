# Milestone 1 baseline ProGuard rules.
# Rules for MediaProjection / RTMP / OpenCV native libs will be added in later milestones.

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep class com.neko.record.data.model.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
