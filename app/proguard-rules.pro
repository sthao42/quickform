# Add project specific ProGuard rules here.

# Keep Room entities and DAOs
-keep class com.sthao.quickform.Form** { *; }
-keep interface com.sthao.quickform.FormDao { *; }

# Keep data classes used in Compose
-keep class com.sthao.quickform.ui.viewmodel.** { *; }

# Keep Compose related classes
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }

# Keep Coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep Coil image loading
-keep class coil.** { *; }

# Keep signature bitmap handling
-keep class android.graphics.Bitmap { *; }
-keep class android.graphics.Canvas { *; }

# General Android optimizations
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# Keep serialization classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
