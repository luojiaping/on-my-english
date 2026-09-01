import java.io.FileInputStream
import java.util.Properties

plugins {
    id("onmyenglish.android.application")
    id("onmyenglish.hilt")
}

val releaseProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) FileInputStream(file).use(::load)
}

android {
    namespace = "com.luojiaping.onmyenglish"

    defaultConfig {
        applicationId = "com.luojiaping.onmyenglish"
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            signingConfig = if (releaseProperties.isNotEmpty() &&
                releaseProperties.getProperty("storeFile")?.let(rootProject::file)?.isFile == true
            ) {
                signingConfigs.create("release") {
                    storeFile = rootProject.file(releaseProperties.getProperty("storeFile"))
                    storePassword = releaseProperties.getProperty("storePassword")
                    keyAlias = releaseProperties.getProperty("keyAlias")
                    keyPassword = releaseProperties.getProperty("keyPassword")
                    storeType = releaseProperties.getProperty("storeType", "PKCS12")
                    enableV1Signing = false
                    enableV2Signing = true
                    enableV3Signing = true
                    enableV4Signing = true
                }
            } else {
                // CI can build a release artifact without receiving the private release key.
                signingConfigs.getByName("debug")
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.navigation.compose)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.feature.settings)
    implementation(projects.feature.stats)
    implementation(projects.feature.study)
    implementation(projects.feature.wordbook)
}
