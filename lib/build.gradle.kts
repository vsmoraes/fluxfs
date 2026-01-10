plugins {
    alias(libs.plugins.kotlin.jvm)

    `java-library`

    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    // S3 SDK
    implementation("aws.sdk.kotlin:s3:1.3.97")

    // Coroutines (if not already added)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Core Kotest dependencies
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

ktlint {
    version.set("1.0.1")
    android.set(false)
}
