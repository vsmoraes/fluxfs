plugins {
    `java-library`
    `maven-publish`
    signing
}

dependencies {
    api(project(":fluxfs-core"))
    testImplementation(testFixtures(project(":fluxfs-core")))

    implementation("aws.sdk.kotlin:s3:1.5.117")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            groupId = "com.vsmoraes.fluxfs"
            artifactId = "fluxfs-s3"

            pom {
                name.set("FluxFS S3")
                description.set("AWS S3 adapter for FluxFS")
                url.set("https://github.com/vsmoraes/fluxfs")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("vsmoraes")
                        name.set("Vinicius Moraes")
                        email.set("vinicius@vsmoraes.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/vsmoraes/fluxfs.git")
                    developerConnection.set("scm:git:ssh://github.com/vsmoraes/fluxfs.git")
                    url.set("https://github.com/vsmoraes/fluxfs")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/vsmoraes/fluxfs")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }

        maven {
            name = "OSSRH"
            url =
                if (version.toString().endsWith("SNAPSHOT")) {
                    uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                } else {
                    uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                }
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    val signingKey = System.getenv("SIGNING_KEY")
    val signingPassword = System.getenv("SIGNING_PASSWORD")
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
}

java {
    withSourcesJar()
    withJavadocJar()
}
