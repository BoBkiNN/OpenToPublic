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

val forge_version = properties["forge_version"] as String
val minecraft_version = properties["minecraft_version"] as String
val shadowCommon = configurations.register("shadowCommon").get()
val common = configurations.register("common").get()

configurations.apply {
    compileClasspath.configure { extendsFrom(common) }
    runtimeClasspath.configure { extendsFrom(common) }
    named("developmentForge").configure { extendsFrom(common) }
}

dependencies {
    configurations.getByName("forge")("net.minecraftforge:forge:$minecraft_version-$forge_version")

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

tasks.processResources {
    filteringCharset = "UTF-8"
    val props = properties.filterValues { it is String }
    inputs.properties(props)
    filesMatching("META-INF/mods.toml") {
        expand(props)
    }
}
