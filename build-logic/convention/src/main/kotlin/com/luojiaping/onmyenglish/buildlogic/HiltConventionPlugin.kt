package com.luojiaping.onmyenglish.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = "com.google.devtools.ksp")

        pluginManager.withPlugin("com.android.base") {
            apply(plugin = "com.google.dagger.hilt.android")
            dependencies {
                "implementation"(libs.findLibrary("hilt-android").get())
                "ksp"(libs.findLibrary("hilt-compiler").get())
                "ksp"(libs.findLibrary("kotlin-metadata").get())
            }
        }
    }
}
