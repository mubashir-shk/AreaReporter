import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                @OptIn(ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)

                // Serialization
                implementation(libs.kotlinx.serialization.json)

                // DateTime
                implementation(libs.kotlinx.datetime)

                // Ktor
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)

                // SQLDelight
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)

                // Koin core only (no koin-compose — voyager-koin dropped)
                implementation(libs.koin.core)

                // Voyager Navigation (without voyager-koin)
                implementation(libs.voyager.navigator)
                implementation(libs.voyager.screenmodel)
                implementation(libs.voyager.transitions)

                // Kamel image loading
                implementation(libs.kamel.image)

                // UUID
                implementation(libs.uuid)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.android)

                // Ktor Android engine
                implementation(libs.ktor.client.android)

                // SQLDelight Android driver
                implementation(libs.sqldelight.android.driver)

                // Koin Android
                implementation(libs.koin.android)
                implementation(libs.koin.androidx.compose)

                // WorkManager
                implementation(libs.workmanager.ktx)

                // Activity
                implementation(libs.activity.compose)
                implementation(libs.core.ktx)
                implementation(libs.lifecycle.runtime.ktx)

                // Accompanist Permissions
                implementation(libs.accompanist.permissions)

                // Coil for Android image loading
                implementation(libs.coil.compose)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.ktor.client.mock)
            }
        }
    }
}

android {
    namespace = "com.areareporter.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("AreaReporterDatabase") {
            packageName.set("com.areareporter.shared.database")
            srcDirs.setFrom("src/commonMain/sqldelight")
        }
    }
}
