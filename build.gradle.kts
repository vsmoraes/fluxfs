import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1" apply false
}

// Shared configuration for all subprojects
subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "java-test-fixtures")

    group = "com.vsmoraes.fluxfs"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }

    dependencies {
        // Coroutines - available to all modules
        add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

        // Testing dependencies - available to all modules
        add("testImplementation", "io.kotest:kotest-runner-junit5:6.0.7")
        add("testImplementation", "io.kotest:kotest-assertions-core:6.0.7")
        add("testImplementation", "io.mockk:mockk:1.14.7")

        // Test fixtures dependencies - available to all modules
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
