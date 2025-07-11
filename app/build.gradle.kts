plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.onlinebankingapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.onlinebankingapp"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}


dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.play:integrity:1.0.2")
    implementation("com.paypal.sdk:paypal-android-sdk:2.16.0")
    implementation("com.github.paymaya:paymaya-android-sdk-v2:2.1.0")
    implementation("com.stripe:stripe-android:20.46.0")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.2")
    implementation ("com.orhanobut:dialogplus:1.11@aar")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.cardview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

configurations.all {
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-runtime")
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlinx" && requested.name == "kotlinx-serialization-runtime") {
            useVersion("1.6.2")
        }
    }
}
