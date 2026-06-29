# Add project specific ProGuard rules here.

# --- Room / Database Rules ---
# Room uses reflection to instantiate entities and DAOs.
-keep class com.sthao.quickform.FormEntry { *; }
-keep class com.sthao.quickform.FormImage { *; }
-keep class com.sthao.quickform.StationsItemSectionEntity { *; }
-keep interface com.sthao.quickform.FormDao { *; }
-keep class com.sthao.quickform.FormEntryWithImagesAndSections { *; }
-keep class com.sthao.quickform.FormListItem { *; }

# --- UI / ViewModel Rules ---
# Keep the Factory because it is often instantiated via reflection by the ViewModelProvider.
-keep class com.sthao.quickform.ui.viewmodel.FormViewModelFactory { *; }

# Keep data class members in ViewModels to prevent issues if they are used in
# generic operations or state restoration.
-keepclassmembers class com.sthao.quickform.ui.viewmodel.** { *; }

# --- Library Specific Rules ---
# Note: Modern versions of Compose, Lifecycle, Coroutines, and Room
# usually provide their own ProGuard rules bundled in the AAR.
# We only add rules here if there are specific runtime issues.

# AndroidX / Compose / Lifecycle: Usually handled by library AAR rules.
# If you encounter issues with ProGuard stripping essential components,
# you can uncomment specific ones below, but avoiding '-keep class ...** { *; }'
# is better for optimization.

# Coroutines: Bundled rules are usually sufficient.
# -keepnames class kotlinx.coroutines.internal.MainDispatcherLoader { *; }

# Coil: Bundled rules are usually sufficient.

# --- General System / Platform Rules ---
# Optimization settings
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# Serialization
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep attributes for better stack traces in crash reports
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,Exceptions,AnnotationDefault,*Annotation*
-renamesourcefileattribute SourceFile
