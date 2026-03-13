import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "com.tengu.app.buildlogic"

// Configure the build-logic plugins to target JDK 17
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.composeCompiler.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "tengu.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("kotlinMultiplatformLibrary") {
            id = "tengu.kmp.library"
            implementationClass = "KotlinMultiplatformLibraryConventionPlugin"
        }
        register("composeMultiplatform") {
            id = "tengu.compose.multiplatform"
            implementationClass = "ComposeMultiPlatformConventionPlugin"
        }
        register("projectFeatureKmp") {
            id = "tengu.project.feature.kmp"
            implementationClass = "ProjectFeatureKmpConventionPlugin"
        }
    }
}
