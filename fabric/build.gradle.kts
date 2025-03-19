plugins {
    `java-library`
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

val fabric_loader_version = properties["fabric_loader_version"] as String
val shadowCommon = configurations.register("shadowCommon").get()
val common = configurations.register("common").get()

configurations.apply {
    compileClasspath.configure { extendsFrom(common) }
    runtimeClasspath.configure { extendsFrom(common) }
    named("developmentFabric").configure { extendsFrom(common) }
}

repositories {
    maven("https://maven.terraformersmc.com/")
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${fabric_loader_version}")

    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":common", configuration = "transformProductionFabric")) {
        isTransitive = false
    }
    shadowCommon(project(path = ":common", configuration = "includeJar"))
    implementation(project(path = ":common", configuration = "includeJar"))

    // for runtime
    modRuntimeOnly("com.terraformersmc:modmenu:11.0.3") // ModMenu for runtime
    modRuntimeOnly(fabricApi.module("fabric-api", "0.115.2+1.21.1"))
}

version = properties["mod_version"]!! as String
val semVer = (version as String).split("-")[0]

println("Fabric mod version: $version ($semVer)")

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
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}
