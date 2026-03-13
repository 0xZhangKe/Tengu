plugins {
    id("tengu.android.application")
    id("tengu.compose.multiplatform")
}

android {
    namespace = "com.tengu.app"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        targetSdk = 36
        applicationId = "com.tengu.app"
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(path = ":framework"))
    implementation(project(path = ":app-hosting"))
    implementation(project(":feature:acp"))

    implementation(compose.material3)
    implementation(compose.components.resources)

    implementation(libs.bundles.androidx.activity)
    implementation(libs.bundles.androidx.nav3)

    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.jetbrains.material3)
}
