plugins {
    id("fluxfs.publishing-conventions")
    `java-test-fixtures`
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)

    testFixturesImplementation(libs.kotest.runner.junit5)
    testFixturesImplementation(libs.kotest.assertions.core)
    testFixturesImplementation(libs.mockk)
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "fluxfs-core"
            pom {
                name.set("FluxFS Core")
                description.set("Core interfaces and contracts for FluxFS - a Kotlin filesystem abstraction library")
            }
        }
    }
}
