plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.player6514.coc"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.player6514.coc"
        minSdk = 24
        targetSdk = 34
        versionCode = (project.findProperty("verCode") as String? ?: "1").toInt()
        versionName = (project.findProperty("verName") as String? ?: "1.0")
    }

    signingConfigs {
        create("release") {
            val ks = file("../keystore.jks")
            if (ks.exists()) {
                storeFile = ks
                storePassword = "coc123456"
                keyAlias = "coc"
                keyPassword = "coc123456"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (file("../keystore.jks").exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}


