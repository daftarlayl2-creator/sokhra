# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep the Support library styles
-keep class android.support.v7.app.** { *; }
-keep class android.support.v4.app.** { *; }
-keep class android.support.annotation.** { *; }

# Keep the core framework classes
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application

# Keep the Kotlin metadata
-keepattributes Signature,KotlinMetadata
-keep class kotlin.Metadata { *; }
