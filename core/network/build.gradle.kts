plugins {
    id("onmyenglish.android.library")
    id("onmyenglish.hilt")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.core.common)
}
