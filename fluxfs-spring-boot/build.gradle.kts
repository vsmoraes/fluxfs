import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
    id("org.springframework.boot")
}

afterEvaluate {
    tasks
        .matching {
            it.name.contains("ktlint", ignoreCase = true)
        }.configureEach {
            enabled = false
        }
}

dependencies {
    api(project(":fluxfs-core"))
    compileOnly(project(":fluxfs-s3"))
    compileOnly(project(":fluxfs-local"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    compileOnly("aws.sdk.kotlin:s3:1.5.122")
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}

tasks.withType<Test> {
    useJUnitPlatform()
}
