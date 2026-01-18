plugins {
    id("fluxfs.publishing-conventions")
    `java-test-fixtures`
}

dependencies {
    api(project(":fluxfs-core"))
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
    testImplementation(testFixtures(project(":fluxfs-core")))
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "fluxfs-local"
            pom {
                name.set("FluxFS Local")
                description.set("Local filesystem adapter for FluxFS")
            }
        }
    }
}
