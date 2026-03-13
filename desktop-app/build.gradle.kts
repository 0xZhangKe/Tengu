import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
}

dependencies {
    implementation(project(":framework"))

    implementation(compose.desktop.currentOs)

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
