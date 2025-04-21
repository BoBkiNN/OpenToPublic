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

val minecraftVersion = properties["minecraft_version"] as String
val parchmentVersion = properties["parchment_version"] as String
val archivesBaseName = properties["archives_base_name"] as String
val modVersion = properties["mod_version"] as String

architectury {
    minecraft = minecraftVersion
}

version = modVersion

subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")
    apply(plugin = "me.modmuss50.mod-publish-plugin")

    base.archivesName = "${archivesBaseName}-${name}-${minecraftVersion}"
    version = modVersion

    val loom = extensions.getByType(LoomGradleExtensionAPI::class)

    loom.silentMojangMappingsLicense()

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        withSourcesJar()
    }

    tasks.withType(JavaCompile::class) {
        options.release = 17
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://maven.parchmentmc.org")
    }

    dependencies {
        configurations.getByName("minecraft")("com.mojang:minecraft:${minecraftVersion}")
        configurations.getByName("mappings")(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${minecraftVersion}:${parchmentVersion}@zip")
        })
    }
}

publishMods {
    type = ReleaseType.STABLE
    rootProject.file("changes.txt").let {
        if (it.canRead()) changelog = it.readText()
    }
    github {
        tagName = "$modVersion-$minecraftVersion"
        commitish = "master"
        repository = "BoBkiNN/OpenToPublic"
        accessToken = providers.environmentVariable("GH_TOKEN")
        displayName = "OpenToPublic $modVersion for $minecraftVersion"
        allowEmptyFiles = true
    }
}
