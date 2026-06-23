plugins {
    alias(miaLibs.plugins.mia.kotlin.multiplatform)
    alias(miaLibs.plugins.kotlinx.serialization)
}

repositories {
    google()
}

kotlin {
    jvmToolchain(17)
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(miaLibs.kotlinx.coroutines)
                implementation(miaLibs.kotlinx.serialization.json)
                implementation(miaLibs.kotlinx.serialization.cbor)
                implementation(projects.sqliteKt)
            }
        }
    }
}