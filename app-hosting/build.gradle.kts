plugins {
    id("tengu.project.feature.kmp")
    id("kotlin-parcelize")
}

android {
    namespace = "com.tengu.app.hosting"
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "TenguKit"
            isStatic = true
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(path = ":framework"))
                implementation(project(path = ":feature:acp"))
                implementation(project(path = ":common:module-visitor"))
                implementation(project(path = ":common:repository"))

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
                implementation(compose.preview)

                implementation(libs.androidx.core.ktx)
                implementation(libs.bundles.androidx.activity)
            }
        }
    }
}

compose {
    resources {
        publicResClass = false
        packageOfResClass = "com.tengu.app.hosting"
        generateResClass = always
    }
}
