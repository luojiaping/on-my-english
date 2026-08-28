package com.luojiaping.onmyenglish.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = "com.android.application")
        apply(plugin = "onmyenglish.android.compose")

        extensions.configure<ApplicationExtension> {
            configureAndroid(this)
            defaultConfig.targetSdk = 37
            testOptions.animationsDisabled = true
        }

        dependencies {
            "implementation"(libs.findLibrary("androidx-core-ktx").get())
            "implementation"(libs.findLibrary("androidx-activity-compose").get())
            "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
            "testImplementation"(libs.findLibrary("junit4").get())
        }
    }
}
