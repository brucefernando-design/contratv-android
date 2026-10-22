# Keep Retrofit and Gson models
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.contratv.player.api.models.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
