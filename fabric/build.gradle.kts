plugins {
    id("fabric-loom") version "1.9-SNAPSHOT"
    `java-library`
    id("io.freefair.lombok") version "8.11"
}

version = properties["mod_version"]!! as String
val semVer = (version as String).split("-")[0]
group = properties["maven_group"]!!


project.ext.set("archivesBaseName", properties["archives_base_name"])

tasks.withType(JavaCompile::class) {
    options.release = 21
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    withSourcesJar()
}

println("Fabric mod version: $version ($semVer)")

//loom.mods {
//    create("opentopublic") {
//        sourceSet(sourceSets.getByName("main"))
//    }
//}

dependencies {
    val includeJar = configurations.create("includeJar")
    includeJar(files("../libs/WaifUPnP-1.2.0.jar"))
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${properties["yarn_mappings"]}:v2")
    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    configurations.implementation.configure{extendsFrom(configurations.getByName("includeJar"))}
}

tasks.jar {
    val dependencies = configurations.getByName("includeJar").map {
        if (it.isDirectory) return@map it else return@map zipTree(it)
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(dependencies)
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}

tasks.processResources {
    filteringCharset = "UTF-8"
    val props = mapOf(
        "semVer" to semVer
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props + properties)
    }
}
