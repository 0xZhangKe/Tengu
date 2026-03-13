plugins {
    id("tengu.kmp.library")
}

android {
    namespace = "com.tengu.app.common.module.visitor"
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":framework"))

                implementation(libs.bundles.androidx.nav3)
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
