@file:Suppress("UnstableApiUsage")

import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("dev.architectury.loom") version "1.9-SNAPSHOT" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT"
    java
    id("com.gradleup.shadow") version "8.3.6" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

val minecraft_version = properties["minecraft_version"] as String
val parchment_version = properties["parchment_version"] as String
val archives_base_name = properties["archives_base_name"] as String
val mod_version = properties["mod_version"] as String

architectury {
    minecraft = minecraft_version
}

version = mod_version

subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")
    apply(plugin = "me.modmuss50.mod-publish-plugin")

    base.archivesName = "${archives_base_name}-${name}-${minecraft_version}"
    version = mod_version

    val loom = extensions.getByType(LoomGradleExtensionAPI::class)

    loom.silentMojangMappingsLicense()

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        withSourcesJar()
    }

    tasks.withType(JavaCompile::class) {
        options.release = 21
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://maven.parchmentmc.org")
    }

    dependencies {
        configurations.getByName("minecraft")("com.mojang:minecraft:${minecraft_version}")
        configurations.getByName("mappings")(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${minecraft_version}:${parchment_version}@zip")
        })
    }
}

publishMods {
    type = ReleaseType.STABLE
    rootProject.file("changes.txt").let {
        if (it.canRead()) changelog = it.readText()
    }
    github {
        tagName = "$mod_version-$minecraft_version"
        commitish = "master"
        repository = "BoBkiNN/OpenToPublic"
        accessToken = providers.environmentVariable("GH_TOKEN")
        displayName = "OpenToPublic $mod_version for $minecraft_version"
        allowEmptyFiles = true
    }
}
