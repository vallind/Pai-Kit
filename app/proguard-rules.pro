# ============================================================================
# 单模块 App ProGuard / R8 规则
# release 构建已开启 minify + shrinkResources（见 app/build.gradle.kts）
# ============================================================================

# --- Hilt / Dagger 生成代码 ---
-keep class **_HiltModules { *; }
-keep class **_HiltModules$* { *; }
-keep,allowobfuscation @dagger.hilt.android.HiltAndroidApp class *

# --- Room ---
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# --- Retrofit / OkHttp ---
# 这两个库自带 consumer-rules，通常足够；这里仅 safe-keep 序列化 DTO
-keep,allowobfuscation,allowshrinking @kotlinx.serialization.Serializable class **

# --- Kotlinx Serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# --- 反射调用兜底（如有业务自定义反射再细化）---
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
