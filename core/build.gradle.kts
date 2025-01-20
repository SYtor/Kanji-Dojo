import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("org.jetbrains.compose")
    alias(libs.plugins.build.config)
    id("app.cash.sqldelight")
    id("com.mikepenz.aboutlibraries.plugin")
}

kotlin {

    jvm()
    androidTarget()

    jvmToolchain(17)

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.ui)
                api(compose.foundation)
                api(compose.material)
                api(compose.material3)
                api(compose.runtime)
                api(compose.materialIconsExtended)
                api(compose.components.resources)

                api(libs.koin.core)
                api(libs.koin.compose)
                api(libs.koin.compose.viewmodel)

                api(libs.kotlinx.datetime)
                api(libs.kotlinx.serialization.json)
                implementation(libs.kotlin.reflect)

                implementation(libs.datastore.preferences.core)
                implementation(libs.wanakana.core)

                api(libs.ktor.client.core)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.auth)

                api(libs.aboutlibraries.core)
                implementation(libs.aboutlibraries.compose.m3)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.sqldelight.android.driver)

                api(libs.lifecycle.viewmodel.ktx)
                api(libs.lifecycle.livedata.ktx)

                implementation(libs.work.runtime.ktx)

                api(libs.koin.android)
                api(libs.koin.androidx.compose)

                implementation(libs.navigation.compose)
                api(libs.activity.compose)
                api(libs.datastore.preferences)
                api(compose.uiTooling)

                api(libs.core.ktx)
                api(libs.appcompat)
                implementation(libs.media3.exoplayer)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.sqldelight.jvm.sqlite.driver)
                implementation(libs.ktor.server.netty)
            }
        }
    }
}

compose.resources {
    generateResClass = always
    packageOfResClass = "ua.syt0r.kanji"
    publicResClass = true
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

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        getByName("release") {
            consumerProguardFile("consumer-rules.pro")
        }
    }

}

val macOsBundleId = "ua.syt0r.kanji-dojo"

compose.desktop {
    application {
        mainClass = "ua.syt0r.kanji.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            packageName = "Kanji Dojo"
            packageVersion = AppVersion.desktopAppVersion
            modules("jdk.unsupported", "java.sql")

            windows {
                upgradeUuid = "12c852a8-6e21-41a7-bd47-3bec9ff5c5df"
                iconFile.set(File("windows_icon.ico"))
                menu = true
                shortcut = true
            }

            macOS {
                bundleID = macOsBundleId
                iconFile.set(File("mac_icon.icns"))
            }

        }
    }
}

buildConfig {

    packageName = "ua.syt0r.kanji"

    buildConfigField("versionCode", AppVersion.versionCode.toLong())
    buildConfigField("versionName", AppVersion.versionName)
    buildConfigField("appDataAssetName", AppAssets.AppDataAssetFileName)
    buildConfigField("appDataDatabaseVersion", AppAssets.AppDataDatabaseVersion)

    val kanaVoiceFieldName = "kanaVoiceAssetName"
    sourceSets.getByName("androidMain") {
        buildConfigField(
            name = kanaVoiceFieldName,
            value = AppAssets.KanaVoice1AndroidFileName
        )
    }
    sourceSets.getByName("jvmMain") {
        buildConfigField(
            name = kanaVoiceFieldName,
            value = AppAssets.KanaVoice1JvmFileName
        )
        buildConfigField(
            name = "macOsBundleId",
            value = macOsBundleId
        )
    }

}

tasks.withType<Test> {
    useJUnitPlatform()
}

aboutLibraries {
    configPath = "core/credits"
    excludeFields = arrayOf("generated")
}

// Desktop
val prepareAssetsTaskDesktop = task<PrepareAssetsTask>("prepareKanjiDojoAssetsDesktop") {
    platform = PrepareAssetsTask.Platform.Desktop
}
project.tasks.findByName("jvmProcessResources")!!.dependsOn(prepareAssetsTaskDesktop)

// Android
val prepareAssetsTaskAndroid = task<PrepareAssetsTask>("prepareKanjiDojoAssetsAndroid") {
    platform = PrepareAssetsTask.Platform.Android
}
project.tasks.findByName("preBuild")!!.dependsOn(prepareAssetsTaskAndroid)
