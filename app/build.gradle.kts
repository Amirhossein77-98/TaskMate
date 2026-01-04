plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.amirsteinbeck.taskmate"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.amirsteinbeck.taskmate"
        minSdk = 29
        targetSdk = 36
        versionCode = 41009
        versionName = "4.10.9"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


        dependenciesInfo {
            includeInApk = false
            includeInBundle = false
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.material3)
    implementation(libs.material)
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("it.xabaras.android:recyclerview-swipedecorator:1.4")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}