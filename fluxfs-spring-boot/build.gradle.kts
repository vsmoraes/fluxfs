plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":fluxfs-core"))
    compileOnly(project(":fluxfs-s3"))
    compileOnly(project(":fluxfs-local"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-autoconfigure")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    compileOnly("aws.sdk.kotlin:s3:1.5.122")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
