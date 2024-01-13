import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.LONG
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("com.codingfeline.buildkonfig")
    id("app.cash.sqldelight")
}


kotlin {
    jvm()
    android()
    sourceSets {
        val koinVersion = "3.2.0"
        val commonMain by getting {
            dependencies {
                api(compose.ui)
                api(compose.foundation)
                api(compose.material)
                api(compose.material3)
                api(compose.runtime)
                api(compose.materialIconsExtended)
                api("io.insert-koin:koin-core:$koinVersion")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
                implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.21")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("app.cash.sqldelight:android-driver:2.0.0")

                val lifecycleVersion = "2.7.0"
                api("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
                api("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")

                implementation("androidx.work:work-runtime-ktx:2.9.0")

                api("io.insert-koin:koin-android:$koinVersion")
                api("io.insert-koin:koin-androidx-compose:$koinVersion")

                implementation("androidx.navigation:navigation-compose:2.7.6")
                api("androidx.activity:activity-compose:1.8.2")
                api("androidx.datastore:datastore-preferences:1.0.0")
                api(compose.uiTooling)

                api("androidx.core:core-ktx:1.12.0")
                api("androidx.appcompat:appcompat:1.6.1")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("app.cash.sqldelight:sqlite-driver:2.0.0")
            }
        }
    }
}

sqldelight {
    databases {
        create("AppDataDatabase") {
            packageName.set("ua.syt0r.kanji.core.app_data.db")
            srcDirs("src/commonMain/sqldelight_app_data")
        }
        create("UserDataDatabase") {
            packageName.set("ua.syt0r.kanji.core.user_data.db")
            srcDirs("src/commonMain/sqldelight_user_data")
        }
    }
}

android {
    namespace = "ua.syt0r.kanji.core"

    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }

    sourceSets["main"].apply {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        assets.srcDir("src/commonMain/resources")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }

}

compose {
    kotlinCompilerPlugin.set("androidx.compose.compiler:compiler:1.5.2")
}

compose.desktop {
    application {
        mainClass = "ua.syt0r.kanji.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "kanji"
            packageVersion = AppVersion.desktopAppVersion
        }
    }
}

buildkonfig {
    packageName = "ua.syt0r.kanji"
    defaultConfigs {
        buildConfigField(LONG, "versionCode", AppVersion.versionCode.toString())
        buildConfigField(STRING, "versionName", AppVersion.versionName)
    }
}