package com.luojiaping.onmyenglish.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = "com.android.library")

        extensions.configure<LibraryExtension> {
            configureAndroid(this)
            namespace = defaultNamespace()
            defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        dependencies {
            "implementation"(libs.findLibrary("androidx-core-ktx").get())
            "testImplementation"(libs.findLibrary("junit4").get())
        }
    }
}
