import com.tengu.app.configureKotlinAndroid
import com.tengu.app.kotlinMultiplatform
import com.tengu.app.libraryExtension
import com.tengu.app.libs
import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinMultiplatformLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }
            libraryExtension {
                configureKotlinAndroid(this)
            }
            kotlinMultiplatform {
                compilerOptions {
                    freeCompilerArgs.add("-Xcontext-parameters")
                    freeCompilerArgs.add("-XXLanguage:+ExplicitBackingFields")
                }
                androidTarget()
                iosX64()
                iosArm64()
                iosSimulatorArm64()
                jvm("desktop")
                sourceSets.apply {
                    commonMain {
                        dependencies {
                            implementation(libs.findLibrary("kotlinx-serialization-core").get())
                            implementation(libs.findLibrary("kotlinx-serialization-json").get())
                        }
                    }
                    targets.configureEach {
                        compilations.configureEach {
                            compileTaskProvider.configure {
                                compilerOptions {
                                    // https://youtrack.jetbrains.com/issue/KT-61573
                                    freeCompilerArgs.add("-Xexpect-actual-classes")
                                    freeCompilerArgs.add("-Xcontext-parameters")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
