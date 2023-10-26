pluginManagement.repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://maven.architectury.dev/")
    maven("https://maven.minecraftforge.net/")
    gradlePluginPortal()
}

plugins {
    `gradle-enterprise`
}

gradleEnterprise.buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}

include(
    "modules:1.20.2-multiloader",
    "modules:1.20.2-multiloader:common",
    "modules:1.20.2-multiloader:fabric",
    "modules:1.20.2-multiloader:forge"
)

include(
    "modules:1.20.2-multiloader-mixins",
    "modules:1.20.2-multiloader-mixins:common",
    "modules:1.20.2-multiloader-mixins:fabric",
    "modules:1.20.2-multiloader-mixins:forge"
)

rootProject.name = "ExampleMod"
