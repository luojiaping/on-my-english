plugins {
    id("onmyenglish.android.library")
    id("onmyenglish.hilt")
    id("onmyenglish.room")
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.model)
}
