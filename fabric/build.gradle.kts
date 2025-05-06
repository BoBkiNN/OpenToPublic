import me.modmuss50.mpp.ReleaseType

plugins {
    `java-library`
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

val minecraftVersion = properties["minecraft_version"] as String
val fabricLoaderVersion = properties["fabric_loader_version"] as String
val shadowCommon = configurations.register("shadowCommon").get()
val common = configurations.register("common").get()

configurations.apply {
    compileClasspath.configure { extendsFrom(common) }
    runtimeClasspath.configure { extendsFrom(common) }
    named("developmentFabric").configure { extendsFrom(common) }
    all {
        resolutionStrategy {
            force("net.fabricmc:fabric-loader:$fabricLoaderVersion")
        }
    }
}

repositories {
    maven("https://maven.terraformersmc.com/")
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")

    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":common", configuration = "transformProductionFabric")) {
        isTransitive = false
    }
    shadowCommon(project(path = ":common", configuration = "includeJar"))
    implementation(project(path = ":common", configuration = "includeJar"))

    // for runtime
    modRuntimeOnly("com.terraformersmc:modmenu:4.1.2") // ModMenu for runtime
    modRuntimeOnly(fabricApi.module("fabric-api", "0.77.0+1.19.2"))
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
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}

publishMods {
    modLoaders.addAll("fabric", "quilt")
    file.set(tasks.remapJar.get().archiveFile)
    type = ReleaseType.STABLE
    rootProject.file("changes.md").let {
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
        displayName = "${project.version} for Fabric $minecraftVersion"
        version = "${project.version}+$minecraftVersion-fabric"
    }

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = providers.gradleProperty("curseforge_id")
        changelogType = "markdown"
        displayName = "${project.version} for Fabric $minecraftVersion"
        minecraftVersions.add(minecraftVersion)
    }
}