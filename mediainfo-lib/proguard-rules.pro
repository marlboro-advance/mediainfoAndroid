# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep MediaInfo classes and their methods
-keep class net.mediaarea.mediainfo.lib.MediaInfo { *; }
-keep class net.mediaarea.mediainfo.lib.MediaInfoUtil { *; }
-keep class net.mediaarea.mediainfo.lib.MediaInfo$** { *; }

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
