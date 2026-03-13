plugins {
    id("tengu.project.feature.kmp")
}

android {
    namespace = "com.tengu.app.feature.acp"
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":framework"))
                implementation(project(":common:module-visitor"))
                implementation(project(":common:repository"))

                implementation(compose.components.resources)

                implementation(libs.jetbrains.lifecycle.viewmodel)
                implementation(libs.bundles.androidx.nav3)
                implementation(libs.jetbrains.material3)
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

compose {
    resources {
        publicResClass = false
        packageOfResClass = "com.tengu.app.feature.acp"
        generateResClass = always
    }
}
