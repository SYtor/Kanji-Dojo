import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {

    jvm()
    jvmToolchain(17)

    sourceSets.commonMain {
        dependencies {
            implementation(project(":core"))
        }
    }

    sourceSets.commonTest {
        dependencies {
            implementation(kotlin("test"))
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTestJUnit4)
        }
    }

    sourceSets.jvmMain {
        dependencies {
            implementation(libs.mockk)
            implementation(libs.javacv.platform)
        }
    }

}
