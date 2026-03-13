import com.tengu.app.kspAll

plugins {
    id("tengu.kmp.library")
    id("com.google.devtools.ksp")
    alias(libs.plugins.room)
}

android {
    namespace = "com.tengu.app.common.repository"
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":framework"))

                implementation(libs.koin.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.multiplatformsettings.core)
                implementation(libs.androidx.room.runtime)
                implementation(libs.ktor.client.core)
                implementation(libs.kotlinx.io.core)
                implementation(libs.okio)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.koin.android)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        iosMain {
            dependencies {
                implementation(libs.androidx.sqlite.bundled)
            }
        }
        getByName("desktopMain").dependencies {
            implementation(libs.androidx.sqlite.bundled)
        }
    }
}

dependencies {
    kspAll(libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}
