import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1" apply false
}

// Shared configuration for all subprojects
subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    group = "com.vsmoraes.fluxfs"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }

    dependencies {
        // Coroutines
        add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

        // Core Kotest dependencies
        add("testImplementation", "io.kotest:kotest-runner-junit5:6.0.7")
        add("testImplementation", "io.kotest:kotest-assertions-core:6.0.7")
    }

    // Use extensions.configure for typed access
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
