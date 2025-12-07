plugins {
    id("com.android.application")
}

android {
    namespace = "ca.gbc.foodspot"

    compileSdk = 35

    defaultConfig {
        applicationId = "ca.gbc.foodspot"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        viewBinding = false
    }
}

dependencies {
    implementation("com.google.android.libraries.places:places:3.3.0")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.cardview:cardview:1.0.0")

    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
}
