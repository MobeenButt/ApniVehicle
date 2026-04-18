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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep all model classes
-keep class com.example.apnivehicle.models.** { *; }

# Keep all repository classes
-keep class com.example.apnivehicle.repository.** { *; }

# Keep all utils classes
-keep class com.example.apnivehicle.utils.** { *; }

# Keep all receivers
-keep class com.example.apnivehicle.receivers.** { *; }

# Keep Gson classes
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep data classes for Gson
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep ViewBinding classes
-keep class com.example.apnivehicle.databinding.** { *; }

# Keep all activities
-keep class com.example.apnivehicle.activities.** { *; }

# Keep all fragments
-keep class com.example.apnivehicle.fragments.** { *; }

# Keep all adapters
-keep class com.example.apnivehicle.adapters.** { *; }

# Keep Material Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Keep AndroidX
-keep class androidx.** { *; }
-dontwarn androidx.**

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Prevent obfuscation of custom classes
-keep,allowobfuscation,allowshrinking class com.example.apnivehicle.** { *; }

# Keep line numbers for debugging
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable