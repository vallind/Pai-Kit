// app/build.gradle.kts — 单模块应用入口；通用配置由 build-logic/ convention plugins 提供
plugins {
    id("pai.android.application")
    id("pai.hilt")
    id("pai.compose")
    id("pai.detekt")
    id("pai.ktlint")
    id("pai.kotlin.test")
    id("pai.kover")
    id("pai.paparazzi")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.pai.app"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Kotlinx
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    // Retrofit / OkHttp
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    // Room / DataStore / Security
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)
    // Coil / Timber / Lottie / Chucker
    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.timber)
    implementation(libs.lottie.compose)
    debugImplementation(libs.chucker.debug)
    releaseImplementation(libs.chucker.noop)

    // M3 卓越线补齐：WindowSizeClass（响应式布局） + 共享元素转场（已在 BOM）
    implementation(libs.androidx.window)
    implementation(libs.androidx.window.core)

    // M3 卓越线补齐：Paparazzi 截图测试
    testImplementation(libs.paparazzi)
}
