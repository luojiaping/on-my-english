plugins {
    id("onmyenglish.android.library")
    id("onmyenglish.android.compose")
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.model)
}
