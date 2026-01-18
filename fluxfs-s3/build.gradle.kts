plugins {
    id("fluxfs.publishing-conventions")
    `java-test-fixtures`
}

dependencies {
    api(project(":fluxfs-core"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.aws.s3)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
    testImplementation(testFixtures(project(":fluxfs-core")))
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "fluxfs-s3"
            pom {
                name.set("FluxFS S3")
                description.set("AWS S3 adapter for FluxFS")
            }
        }
    }
}
