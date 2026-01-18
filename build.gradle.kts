import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1" apply false

    kotlin("plugin.spring") version "1.9.25" apply false
    id("org.springframework.boot") version "3.3.2" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "java-test-fixtures")

    group = "com.vsmoraes.fluxfs"
    version = "1.0.0"

    dependencies {
        add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        add("testImplementation", "io.kotest:kotest-runner-junit5:6.0.7")
        add("testImplementation", "io.kotest:kotest-assertions-core:6.0.7")
        add("testImplementation", "io.mockk:mockk:1.14.7")
        add("testFixturesImplementation", "io.kotest:kotest-runner-junit5:6.0.7")
        add("testFixturesImplementation", "io.kotest:kotest-assertions-core:6.0.7")
        add("testFixturesImplementation", "io.mockk:mockk:1.14.7")
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    extensions.configure<KtlintExtension> {
        version.set("1.0.1")
        android.set(false)
    }
}
