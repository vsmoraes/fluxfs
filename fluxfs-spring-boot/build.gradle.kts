import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("fluxfs.publishing-conventions")
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    api(project(":fluxfs-core"))
    compileOnly(project(":fluxfs-s3"))
    compileOnly(project(":fluxfs-local"))

    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)

    compileOnly(libs.aws.s3)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}

// Disable ktlint for Spring Boot configuration classes
tasks
    .matching { it.name.contains("ktlint", ignoreCase = true) }
    .configureEach { enabled = false }

publishing {
    publications {
        named<MavenPublication>("maven") {
            artifactId = "fluxfs-spring-boot-starter"
            pom {
                name.set("FluxFS Spring Boot Starter")
                description.set("Spring Boot auto-configuration for FluxFS")
            }
        }
    }
}
