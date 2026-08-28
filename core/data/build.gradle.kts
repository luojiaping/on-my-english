plugins {
    id("onmyenglish.android.library")
    id("onmyenglish.hilt")
}

dependencies {
    implementation(projects.core.ai)
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.domain)
    implementation(projects.core.model)
}
