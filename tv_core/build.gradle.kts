plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.1.0"
    id("maven-publish")
}

group = "com.github.turtlepaw"
version = "1.0.1-alpha2"

android {
    namespace = "com.turtlepaw.nearby_settings.tv_core"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }

    lint {
        targetSdk = 35
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.play.services.nearby)

    implementation(libs.kotlinx.serialization.json)

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.37.0")

    // QR Codes (used for DiscoveryDialog)
    implementation("com.github.alexzhirkevich:custom-qr-generator:2.0.0-alpha01")
    implementation("com.google.accompanist:accompanist-drawablepainter:0.37.0")

    // M3 Mobile
    implementation("androidx.compose.material3:material3")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.turtlepaw"
                artifactId = "tv_core"
                version = "1.0"
            }
        }
    }
}
