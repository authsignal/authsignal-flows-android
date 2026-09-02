import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    id("maven-publish")
    alias(libs.plugins.jreleaser)
}

android {
    namespace = "com.authsignal.flows"

    compileSdk = 36

    defaultConfig {
        minSdk = 23
        targetSdk = 36
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

fun getProperty(name: String): String {
    // Falls back to "" (rather than throwing) when unset, so a plain `build`/`test` doesn't
    // require publishing secrets to be configured — only an actual sign/publish task does,
    // and will fail with a proper JReleaser-level error at that point instead.
    return System.getenv(name) ?: gradleLocalProperties(rootDir, providers).getProperty(name) ?: ""
}

val pomGroup: String by project
val pomArtifactId: String by project
val versionName: String by project

project.version = versionName

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = pomGroup
            artifactId = pomArtifactId

            pom {
                name.set("authsignal-flows-android")
                description.set("The official Authsignal Flows SDK for Android.")
                url.set("https://github.com/authsignal/authsignal-flows-android")

                scm {
                    url.set("https://github.com/authsignal/authsignal-flows-android/tree/main")
                    connection.set("scm:git:github.com/authsignal/authsignal-flows-android.git")
                    developerConnection.set("scm:git:ssh://github.com/authsignal/authsignal-flows-android.git")
                }

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/authsignal/authsignal-flows-android/blob/main/LICENSE.md")
                    }
                }

                developers {
                    developer {
                        id.set("Authsignal")
                        name.set("Authsignal")
                    }
                }

                afterEvaluate {
                    from(components["release"])
                }
            }
        }
    }

    repositories {
        maven {
            setUrl(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

jreleaser {
    project {
        java {
            groupId.set("com.authsignal")
            artifactId.set("authsignal-flows-android")
            description.set("Authsignal Flows SDK for Android")
        }
    }
    gitRootSearch = true
    signing {
        setActive("ALWAYS")
        verify = false
        secretKey = getProperty("SIGNING_SECRET_KEY")
        passphrase = getProperty("SIGNING_PASSPHRASE")
    }
    release {
        github {
            skipRelease = true
        }
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    stagingRepository("build/staging-deploy")
                    setActive("ALWAYS")
                    url = "https://central.sonatype.com/api/v1/publisher"
                    username = getProperty("MAVENCENTRAL_USERNAME")
                    password = getProperty("MAVENCENTRAL_PASSWORD")
                    applyMavenCentralRules = false // See: https://github.com/kordamp/pomchecker/issues/21
                    sourceJar = true
                    javadocJar = true
                    sign = true
                }
            }
        }
    }
}

dependencies {
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
}
