# These ProGuard rules will be automatically applied to apps that use this library

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep MediaInfo library classes
-keep class net.mediaarea.mediainfo.lib.** { *; }

# Keep enums
-keepclassmembers enum net.mediaarea.mediainfo.lib.MediaInfo$** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
