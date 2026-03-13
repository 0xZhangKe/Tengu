package com.tengu.app

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.tengu.app.kotlin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.support.delegates.DependencyHandlerDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal val Project.compose
    get() = org.jetbrains.compose.ComposePlugin.Dependencies(this)

internal fun Project.libraryExtension(action: LibraryExtension.() -> Unit) =
    extensions.configure<LibraryExtension>(action)

internal fun Project.applicationExtension(action: ApplicationExtension.() -> Unit) =
    extensions.configure<ApplicationExtension>(action)

internal fun Project.libraryComponentsExtension(action: LibraryAndroidComponentsExtension.() -> Unit) =
    extensions.configure<LibraryAndroidComponentsExtension>(action)

internal fun Project.applicationComponentsExtension(action: ApplicationAndroidComponentsExtension.() -> Unit) =
    extensions.configure<ApplicationAndroidComponentsExtension>(action)

internal fun Project.kotlinCompile(action: KotlinJvmCompile.() -> Unit) =
    tasks.withType<KotlinCompile>().all { action(this) }

internal fun Project.kotlin(action: KotlinProjectExtension.() -> Unit) =
    extensions.configure<KotlinProjectExtension>(action)

internal fun Project.kotlinMultiplatform(action: KotlinMultiplatformExtension.() -> Unit) =
    extensions.configure<KotlinMultiplatformExtension>(action)

internal fun Project.java(action: JavaPluginExtension.() -> Unit) =
    extensions.configure<JavaPluginExtension>(action)

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun DependencyHandler.implementation(dependency: Any) =
    add("implementation", dependency)

fun DependencyHandlerScope.kspAll(dependencyNotation: Any) {
    add("kspAndroid", dependencyNotation)
    add("kspIosSimulatorArm64", dependencyNotation)
    add("kspIosX64", dependencyNotation)
    add("kspIosArm64", dependencyNotation)
    add("kspDesktop", dependencyNotation)
}

fun KotlinMultiplatformExtension.configureCommonMainKsp() {
    sourceSets.named("commonMain").configure {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }
}
