import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeJB)
}

group = "io.github.edwinchang24.shengjidisplay"

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "shengjidisplay"
        browser {
            commonWebpackConfig {
                outputFileName = "shengjidisplay.js"
                devServer =
                    (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                        static =
                            (static ?: mutableListOf()).apply {
                                add(project.rootDir.path)
                                add(project.rootDir.path + "/shared/")
                                add(project.rootDir.path + "/webApp/")
                            }
                        watchOptions = watchOptions?.copy(ignored = "**/**")
                    }
            }
        }
        binaries.executable()
    }
    sourceSets {
        val wasmJsMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(libs.kotlinx.serialization)
            }
        }
    }
}
