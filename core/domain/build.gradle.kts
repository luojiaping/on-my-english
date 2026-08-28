plugins {
    id("onmyenglish.jvm.library")
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.model)
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)
}
