pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    // Kotlin/Native 在配置 iOS target 时会动态添加 ivy 仓库下载 prebuilt 编译器，
    // 这里必须允许项目级仓库生效，否则会出现 kotlin-native-prebuilt 无法解析。
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        maven("https://download.jetbrains.com/kotlin/native/builds")
    }
}

rootProject.name = "JdcrLogCommon"
include(":app")
include(":jdcrlog")
