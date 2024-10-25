pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("multiplatform") version "2.0.20"
        kotlin("android") version "2.0.20"
        kotlin("plugin.serialization") version "2.0.20"
        kotlin("plugin.compose") version "2.0.20"
        id("com.android.application") version "8.5.2"
        id("com.android.library") version "8.5.2"
        id("org.jetbrains.compose") version "1.7.0"
        id("com.google.gms.google-services") version "4.4.2"
        id("com.google.firebase.crashlytics") version "3.0.2"
        id("com.codingfeline.buildkonfig") version "0.13.3"
        id("app.cash.sqldelight") version "2.0.0"
        id("com.mikepenz.aboutlibraries.plugin") version "11.2.0"
    }
}

rootProject.name = "kanji-dojo"
include(":app", ":core")