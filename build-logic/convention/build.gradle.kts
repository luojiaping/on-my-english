import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.luojiaping.onmyenglish.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
    compileOnly(libs.hilt.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
    compileOnly(libs.room.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "onmyenglish.android.application"
            implementationClass = "com.luojiaping.onmyenglish.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "onmyenglish.android.library"
            implementationClass = "com.luojiaping.onmyenglish.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "onmyenglish.android.compose"
            implementationClass = "com.luojiaping.onmyenglish.buildlogic.AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "onmyenglish.android.feature"
            implementationClass = "com.luojiaping.onmyenglish.buildlogic.AndroidFeatureConventionPlugin"
        }
        register("hilt") {
            id = "onmyenglish.hilt"
            implementationClass = "com.luojiaping.onmyenglish.buildlogic.HiltConventionPlugin"
        }
        register("room") {
            id = "onmyenglish.room"
            implementationClass = "com.luojiaping.onmyenglish.buildlogic.RoomConventionPlugin"
        }
        register("jvmLibrary") {
            id = "onmyenglish.jvm.library"
            implementationClass = "com.luojiaping.onmyenglish.buildlogic.JvmLibraryConventionPlugin"
        }
    }
}
