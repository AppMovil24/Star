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
        minSdk = 30
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Firebase.
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Autenticacion con Google.
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.firebase:firebase-auth-ktx")

    // Gson: Serializar objetos para el bundle.
    implementation("com.google.code.gson:gson:2.8.8")

    // Firestore: Almacenamiento para imagenes.
    implementation(libs.firebase.storage.ktx)

    // Picasso: Obtener imagenes almacenadas en firestore a partir URLs persistidas en firebaseDB.
    implementation("com.squareup.picasso:picasso:2.71828")

    // Google maps
    implementation("com.google.android.gms:play-services-maps:17.0.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.6.0") // GeoPoint

    // Notificaciones
    implementation(libs.firebase.messaging)

    // Open streetmap
    implementation("org.osmdroid:osmdroid-android:6.1.10")

}