plugins {
    id("onmyenglish.android.application")
    id("onmyenglish.hilt")
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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.feature.settings)
    implementation(projects.feature.stats)
    implementation(projects.feature.study)
    implementation(projects.feature.wordbook)
}
