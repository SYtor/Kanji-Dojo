pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    val kotlinVersion = "1.9.0"
    val agpVersion = "8.1.2"
    val composeVersion = "1.6.11"

    plugins {
        kotlin("multiplatform") version kotlinVersion
        kotlin("android") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("com.android.application") version agpVersion
        id("com.android.library") version agpVersion
        id("org.jetbrains.compose") version composeVersion
        id("com.google.gms.google-services") version "4.4.0"
        id("com.google.firebase.crashlytics") version "2.9.9"
        id("com.codingfeline.buildkonfig") version "0.13.3"
        id("app.cash.sqldelight") version "2.0.0"
        id("com.mikepenz.aboutlibraries.plugin") version "11.2.0"
    }
}

rootProject.name = "kanji-dojo"
include(":app", ":core")