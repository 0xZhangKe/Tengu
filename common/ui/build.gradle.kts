import org.gradle.kotlin.dsl.implementation

plugins {
    id("tengu.kmp.library")
    id("tengu.compose.multiplatform")
}

android {
    namespace = "com.tengu.app.common.ui"
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":framework"))
                implementation(libs.jetbrains.compose.runtime)
                implementation(libs.jetbrains.compose.ui)
                implementation(libs.jetbrains.compose.foundation)
                implementation(libs.jetbrains.material3)

                implementation(libs.markdownRenderer)
                implementation(libs.markdownRendererM3)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        androidMain {
            dependencies {
                implementation(libs.androidx.core.ktx)
            }
        }
    }
}
