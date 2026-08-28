package com.luojiaping.onmyenglish.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = "org.jetbrains.kotlin.jvm")

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
        extensions.configure<KotlinJvmProjectExtension> {
            compilerOptions.jvmTarget = JvmTarget.JVM_11
        }
        dependencies {
            "testImplementation"(libs.findLibrary("junit4").get())
        }
    }
}
