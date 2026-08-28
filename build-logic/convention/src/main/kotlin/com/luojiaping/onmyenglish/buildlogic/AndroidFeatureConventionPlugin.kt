package com.luojiaping.onmyenglish.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = "onmyenglish.android.library")
        apply(plugin = "onmyenglish.android.compose")
        apply(plugin = "onmyenglish.hilt")

        dependencies {
            "implementation"(libs.findLibrary("androidx-compose-material-icons-extended").get())
            "implementation"(libs.findLibrary("androidx-hilt-navigation-compose").get())
            "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
            "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
            "implementation"(project(":core:common"))
            "implementation"(project(":core:designsystem"))
            "implementation"(project(":core:model"))
            "implementation"(project(":core:ui"))
        }
    }
}
