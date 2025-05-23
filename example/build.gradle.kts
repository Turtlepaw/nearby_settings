plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.nearbysettingsexample"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.nearbysettingsexample"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.1"

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
    // M3 Mobile
    implementation("androidx.compose.material3:material3")

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

    //implementation(project(":tv-core"))
    //implementation("com.github.Turtlepaw:nearby_settings:940eb64417")
    implementation(project(":tv_core"))

    implementation("com.google.accompanist:accompanist-permissions:0.37.0")
}

android.applicationVariants.all {
    outputs.all {
        println(">>> APK for ${this@all.name} variant: ${outputFile}")
    }
}

