import com.tengu.app.applicationExtension
import com.tengu.app.libraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

class ComposeMultiPlatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
            }
            if (pluginManager.hasPlugin("com.android.application")) {
                applicationExtension {
                    buildFeatures {
                        compose = true
                    }
                }
            } else if (pluginManager.hasPlugin("com.android.library")) {
                libraryExtension {
                    buildFeatures {
                        compose = true
                    }
                }
            }
            composeCompiler {
                // Enable 'strong skipping'
                // https://medium.com/androiddevelopers/jetpack-compose-strong-skipping-mode-explained-cbdb2aa4b900

                if (project.providers.gradleProperty("tengu.enableComposeCompilerReports").isPresent) {
                    val composeReports = layout.buildDirectory.map { it.dir("reports").dir("compose") }
                    reportsDestination.set(composeReports)
                    metricsDestination.set(composeReports)
                }
            }
        }
    }

    private fun Project.composeCompiler(block: ComposeCompilerGradlePluginExtension.() -> Unit) {
        extensions.configure<ComposeCompilerGradlePluginExtension>(block)
    }
}
