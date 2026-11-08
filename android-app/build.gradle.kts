plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "it.bwlab.clima"
    compileSdk = 34

    defaultConfig {
        applicationId = "it.bwlab.clima"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "2.0"
    }

    buildTypes {
        debug {
            // Supporto pseudo-locale en-XA per testare i troncamenti
            isPseudoLocalesEnabled = true
        }
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
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
        kotlinCompilerVersion = "1.8.10"
    }
}

dependencies {
    implementation(project(":kotlin-core"))

    // Compose + Material 3 (migrazione dalla M2 legacy)
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")
    implementation("androidx.compose.foundation:foundation:1.4.3")
    implementation("androidx.compose.material:material-icons-extended:1.4.3")
    implementation("androidx.compose.material3:material3:1.1.1")

    // Activity / AppCompat: necessario per la lingua per-app (AppCompatDelegate)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-compose:1.6.1")

    // Lifecycle / ViewModel
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Preferenze: dispositivi salvati + impostazioni
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Pull-to-refresh
    implementation("com.google.accompanist:accompanist-swiperefresh:0.30.1")

    // Navigazione Compose
    implementation("androidx.navigation:navigation-compose:2.6.0")
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:1.8.10")
        // materiale icons-extended innesta dipendenze jdk7/jdk8 duplicate
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }
}
