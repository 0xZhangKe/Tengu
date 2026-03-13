import com.tengu.app.compose
import com.tengu.app.kotlinMultiplatform
import com.tengu.app.kspAll
import com.tengu.app.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

class ProjectFeatureKmpConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("tengu.kmp.library")
                apply("tengu.compose.multiplatform")
                apply("com.google.devtools.ksp")
            }
            kotlinMultiplatform {
                sourceSets.apply {
                    commonMain {
                        dependencies {
                            implementation(compose.runtime)
                            implementation(compose.ui)
                            implementation(compose.foundation)
                            implementation(compose.materialIconsExtended)
                            implementation(compose.material3)

                            implementation(libs.findLibrary("kotlinx-datetime").get())

                            implementation(libs.findLibrary("koin-core").get())
                            implementation(libs.findLibrary("koin-compose").get())
                            implementation(libs.findLibrary("koin-compose-viewmodel").get())
                            implementation(libs.findLibrary("koin-compose-nav3").get())
                            implementation(libs.findLibrary("koin-annotations").get())

                            implementation(libs.findLibrary("ktor-client-core").get())
                            implementation(libs.findLibrary("ktor-client-content-negotiation").get())
                            implementation(libs.findLibrary("ktor-client-serialization-kotlinx-json").get())
                            implementation(libs.findLibrary("ktor-client-logging").get())
                        }
                    }
                    androidMain {
                        dependencies {
                            implementation(libs.findLibrary("koin-android").get())
                            implementation(libs.findLibrary("ktor-client-okhttp").get())
                        }
                    }
                    iosMain {
                        dependencies {
                            implementation(libs.findLibrary("ktor-client-darwin").get())
                        }
                    }
                    getByName("desktopMain").dependencies {
                        implementation(libs.findLibrary("ktor-client-okhttp").get())
                    }
                    targets.configureEach {
                        val isAndroidTarget = platformType == KotlinPlatformType.androidJvm
                        compilations.configureEach {
                            compileTaskProvider.configure {
                                compilerOptions {
                                    if (isAndroidTarget) {
                                        freeCompilerArgs.addAll(
                                            "-P",
                                            "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=com.tengu.app.framework.utils.Parcelize",
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            dependencies {
                kspAll(libs.findLibrary("koin-ksp-compiler").get())
            }
            tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }.configureEach {
                dependsOn("kspCommonMainKotlinMetadata")
            }
        }
    }
}
