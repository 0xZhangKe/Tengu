import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":framework"))
    implementation(project(":common:ui"))

    implementation(compose.desktop.currentOs)
    implementation(libs.jetbrains.material3)
    implementation(libs.bundles.androidx.nav3)
    implementation(libs.jetbrains.lifecycle.viewmodel.compose)
    implementation(libs.kotlin.coroutine.swing)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.compose.nav3)

    implementation(libs.acp)
    runtimeOnly(libs.slf4j.simple)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
        freeCompilerArgs.add("-XXLanguage:+ExplicitBackingFields")
        freeCompilerArgs.add("-Xskip-prerelease-check")
    }
}

compose.desktop {
    application {
        mainClass = "com.tengu.app.desktop.MainKt"
    }
}
