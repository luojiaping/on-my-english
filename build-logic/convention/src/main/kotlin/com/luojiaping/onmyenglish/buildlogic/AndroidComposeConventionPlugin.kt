package com.luojiaping.onmyenglish.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = "org.jetbrains.kotlin.plugin.compose")

        pluginManager.withPlugin("com.android.application") {
            extensions.configure<ApplicationExtension> { configureCompose(this) }
        }
        pluginManager.withPlugin("com.android.library") {
            extensions.configure<LibraryExtension> { configureCompose(this) }
        }

        dependencies {
            val bom = platform(libs.findLibrary("androidx-compose-bom").get())
            "implementation"(bom)
            "androidTestImplementation"(bom)
            "implementation"(libs.findLibrary("androidx-compose-ui").get())
            "implementation"(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            "implementation"(libs.findLibrary("androidx-compose-material3").get())
            "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())
        }
    }

    private fun configureCompose(commonExtension: CommonExtension) {
        commonExtension.buildFeatures.compose = true
    }
}
