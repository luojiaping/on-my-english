plugins {
    id("onmyenglish.android.feature")
}

dependencies {
    implementation(projects.core.ai)
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(libs.coil.compose)
}
