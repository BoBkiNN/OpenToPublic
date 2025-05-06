import me.modmuss50.mpp.ReleaseType

plugins {
    `java-library`
    id("com.gradleup.shadow")
}

loom {
    forge {
        mixinConfig("opentopublic.mixins.json")
    }
}

architectury {
    platformSetupLoomIde()
    forge()
}

val forgeVersion = properties["forge_version"] as String
val minecraftVersion = properties["minecraft_version"] as String
val shadowCommon = configurations.register("shadowCommon").get()
val common = configurations.register("common").get()

configurations.apply {
    compileClasspath.configure { extendsFrom(common) }
    runtimeClasspath.configure { extendsFrom(common) }
    named("developmentForge").configure { extendsFrom(common) }
}

dependencies {
    configurations.getByName("forge")("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")

    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":common", configuration = "transformProductionForge")) {
        isTransitive = false
    }
    shadowCommon(files("../libs/WaifUPnP-1.2.0.jar"))
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}

tasks.shadowJar {
    configurations = listOf(shadowCommon)
}

tasks.remapJar {
    inputFile = tasks.shadowJar.get().archiveFile
    dependsOn(tasks.shadowJar)
}

tasks.register<Copy>("copyIcon") {
    from(project(":common").layout.projectDirectory.file("src/main/resources/icon.png"))
    into(layout.buildDirectory.dir("resources/main"))
}

tasks.processResources {
    dependsOn("copyIcon")
    filteringCharset = "UTF-8"
    val props = properties.filterValues { it is String }.toMutableMap()
    props["fullVersion"] = "$version+$minecraftVersion-${project.name}"
    inputs.properties(props)
    filesMatching("META-INF/mods.toml") {
        expand(props)
    }
}

publishMods {
    modLoaders.addAll("forge", "neoforge")
    file.set(tasks.remapJar.get().archiveFile)
    type = ReleaseType.STABLE
    rootProject.file("changes.txt").let {
        if (it.canRead()) changelog = it.readText()
    }

    github {
        accessToken = providers.environmentVariable("GH_TOKEN")
        parent(rootProject.tasks.named("publishGithub"))
    }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = providers.gradleProperty("modrinth_id")
        minecraftVersions.add(minecraftVersion)
        displayName = "${project.version} for Forge $minecraftVersion"
        version = "${project.version}+$minecraftVersion-forge"
    }

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = providers.gradleProperty("curseforge_id")
        changelogType = "markdown"
        displayName = "${project.version} for Forge $minecraftVersion"
        minecraftVersions.add(minecraftVersion)
    }
}
