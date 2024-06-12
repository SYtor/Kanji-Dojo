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
    id("com.mikepenz.aboutlibraries.plugin")
}

kotlin {
    jvm()
    android()
    sourceSets {
        val koinVersion = "3.2.0"
        val ktorVersion = "2.3.9"
        val commonMain by getting {
            dependencies {
                api(compose.ui)
                api(compose.foundation)
                api(compose.material)
                api(compose.material3)
                api(compose.runtime)
                api(compose.materialIconsExtended)
                api("io.insert-koin:koin-core:$koinVersion")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.21")
                implementation("dev.esnault.wanakana:wanakana-core:1.1.1")

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")

                api("com.mikepenz:aboutlibraries-core:11.2.0")
                implementation("com.mikepenz:aboutlibraries-compose-m3:11.2.0")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("app.cash.sqldelight:android-driver:2.0.0")

                val lifecycleVersion = "2.8.1"
                api("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
                api("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")

                implementation("androidx.work:work-runtime-ktx:2.9.0")

                api("io.insert-koin:koin-android:$koinVersion")
                api("io.insert-koin:koin-androidx-compose:$koinVersion")

                implementation("androidx.navigation:navigation-compose:2.7.7")
                api("androidx.activity:activity-compose:1.9.0")
                api("androidx.datastore:datastore-preferences:1.1.1")
                api(compose.uiTooling)

                api("androidx.core:core-ktx:1.13.1")
                api("androidx.appcompat:appcompat:1.7.0")
                implementation("androidx.media3:media3-exoplayer:1.3.1")
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

    buildTypes {
        getByName("release") {
            consumerProguardFile("consumer-rules.pro")
        }
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

    val kanaVoiceFieldName = "kanaVoiceAssetName"

    defaultConfigs {
        buildConfigField(LONG, "versionCode", AppVersion.versionCode.toString())
        buildConfigField(STRING, "versionName", AppVersion.versionName)
        buildConfigField(
            type = STRING,
            name = "appDataAssetName",
            value = PrepareKanjiDojoAssetsTask.AppDataAssetFileName
        )
        buildConfigField(
            type = LONG,
            name = "appDataDatabaseVersion",
            value = PrepareKanjiDojoAssetsTask.AppDataDatabaseVersion.toString()
        )
        buildConfigField(
            type = STRING,
            name = kanaVoiceFieldName,
            value = "Will be overridden in target config"
        )
    }

    targetConfigs {
        create("android") {
            buildConfigField(
                type = STRING,
                name = kanaVoiceFieldName,
                value = PrepareKanjiDojoAssetsTask.KanaVoice1AndroidFileName
            )
        }
        create("jvm") {
            buildConfigField(
                type = STRING,
                name = kanaVoiceFieldName,
                value = PrepareKanjiDojoAssetsTask.KanaVoice1JvmFileName
            )
        }
    }
}

aboutLibraries { configPath = "core/credits" }

// Desktop
val prepareAssetsTaskDesktop = task<PrepareKanjiDojoAssetsTask>("prepareKanjiDojoAssetsDesktop") {
    platform = PrepareKanjiDojoAssetsTask.Platform.Desktop
}
project.tasks.findByName("jvmProcessResources")!!.dependsOn(prepareAssetsTaskDesktop)

// Android
val prepareAssetsTaskAndroid = task<PrepareKanjiDojoAssetsTask>("prepareKanjiDojoAssetsAndroid") {
    platform = PrepareKanjiDojoAssetsTask.Platform.Android
}
project.tasks.findByName("preBuild")!!.dependsOn(prepareAssetsTaskAndroid)
