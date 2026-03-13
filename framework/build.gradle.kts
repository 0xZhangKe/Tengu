plugins {
    id("tengu.project.feature.kmp")
    id("kotlin-parcelize")
}

android {
    namespace = "com.tengu.app.framework"
    sourceSets {
        getByName("main") {
            res.srcDirs("src/commonMain/res")
            resources.srcDirs("src/commonMain/resources")
        }
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.components.resources)

                implementation(libs.androidx.annotation)
                implementation(libs.jetbrains.material3)

                implementation(libs.jetbrains.lifecycle.runtime)
                implementation(libs.jetbrains.lifecycle.viewmodel)
                implementation(libs.kotlinx.io.core)
                implementation(libs.okio)

                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.serialization.kotlinx.json)
                implementation(libs.bundles.androidx.nav3)
                implementation(libs.kermit)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        androidMain {
            dependencies {
                implementation(compose.uiTooling)
                implementation(compose.preview)

                implementation(libs.androidx.core.ktx)
                implementation(libs.bundles.androidx.activity)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.okhttp)
                implementation(libs.okhttp.logging)
            }
        }
        androidUnitTest {
            dependencies {
            }
        }
        androidInstrumentedTest {
            dependencies {
            }
        }
        iosMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        desktopMain {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.okhttp)
            }
        }
    }
}

compose {
    resources {
        publicResClass = true
        packageOfResClass = "com.tengu.app.framework"
        generateResClass = always
    }
}
