plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.appmovil24.starproyect"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.appmovil24.starproyect"
        minSdk = 29
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Import the Firebase BoM
    //implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    //implementation("com.google.firebase:firebase-analytics")
    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries

    //--------------------------------------------------------------
    // implementation("androidx.credentials:credentials:<latest version>")
    // implementation("androidx.credentials:credentials-play-services-auth:<latest version>")
    // implementation("com.google.android.libraries.identity.googleid:googleid:<latest version>")
    // implementation("androidx.legacy:legacy-support-v4:1.0.0")
    // implementation("androidx.appcompat:appcompat:1.6.1")
    // implementation("androidx.browser:browser:1.0.0")
    // implementation("androidx.cardview:cardview:1.0.0")
    // implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // implementation("com.google.android.material:material:1.9.0")
    // implementation("androidx.activity:activity-ktx:1.7.2")

    // Import the BoM for the Firebase platform
    // implementation(platform("com.google.firebase:firebase-bom:32.3.1"))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    // implementation("com.google.firebase:firebase-auth-ktx")

    // [START gradle_firebase_ui_auth]
    // implementation("com.firebaseui:firebase-ui-auth:8.0.2")

    // Required only if Facebook login support is required
    // Find the latest Facebook SDK releases here: https://goo.gl/Ce5L94
    //implementation("com.facebook.android:facebook-android-sdk:4.42.0")

    //---------------------------------------------------------------
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-analytics")

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth-ktx")

    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.6.0")

}