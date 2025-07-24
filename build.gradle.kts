plugins {
    alias(libs.plugins.kotlin.multiplatform)
    `maven-publish`
}

group = "me.dvyy.sqlite"
version = "0.0.1"

repositories {
    mavenCentral()
    google()
}

kotlin {
    jvm()

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }

    sourceSets {
        commonMain {
            dependencies {
                dependencies {
                    api(libs.androidx.sqlite)
                    implementation(libs.kotlinx.coroutines.core)
                }
            }
        }
    }
}

publishing {
    repositories {
        maven {
            name = "mineinabyss"
            url = uri("https://repo.mineinabyss.com/releases")
            credentials(PasswordCredentials::class)
        }
    }
}
