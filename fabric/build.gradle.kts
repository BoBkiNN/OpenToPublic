@file:Suppress("UnstableApiUsage")

plugins {
    id("fabric-loom") version "1.9-SNAPSHOT"
    `java-library`
    id("io.freefair.lombok") version "8.11"
}

version = properties["mod_version"]!! as String
val semVer = (version as String).split("-")[0]
group = properties["maven_group"]!!
val baseName = properties["archives_base_name"] as String

tasks.withType(JavaCompile::class) {
    options.release = 21
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    withSourcesJar()
}

println("Fabric mod version: $version ($semVer)")


repositories {
    maven("https://maven.parchmentmc.org") {
        name = "ParchmentMC"
    }
}

dependencies {
    val includeJar = configurations.create("includeJar")
    includeJar(files("../libs/WaifUPnP-1.2.0.jar"))
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    val maps = loom.layered() {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21.1:2024.11.17@zip")
    }
    mappings(maps)
    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    configurations.implementation.configure{extendsFrom(configurations.getByName("includeJar"))}
}

base {
    archivesName = baseName
}

tasks.jar {
    val dependencies = configurations.getByName("includeJar").map {
        if (it.isDirectory) return@map it else return@map zipTree(it)
    }

    archiveBaseName = baseName

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

val assetsDir = layout.projectDirectory.dir("src/main/resources/assets")
val resourcePacksDir = layout.projectDirectory.dir("run/resourcepacks")

tasks {
    val generatePackMcMeta by creating {
        group = "other"
        description = "Generates the pack.mcmeta file."

        val packMcMetaContent = """
            {
                "pack": {
                    "pack_format": 34,
                    "description": "Fix to runIde missing resources"
                }
            }
        """.trimIndent()

        val outputDir = layout.buildDirectory.dir("generated/packmcmeta")

        doLast {
            val packMcMetaFile = outputDir.get().file("pack.mcmeta").asFile
            packMcMetaFile.parentFile.mkdirs()
            packMcMetaFile.writeText(packMcMetaContent)
            println("Generated pack.mcmeta at: ${packMcMetaFile.absolutePath}")
        }

        outputs.file(outputDir.get().file("pack.mcmeta"))
    }

    val createResourcePack by creating(Zip::class) {
        group = "other"
        description = "Creates a resource pack archive from the assets folder and pack.mcmeta."

        from(assetsDir) {
            into("assets")
        }

        dependsOn(generatePackMcMeta)
        from(generatePackMcMeta.outputs.files.singleFile) {
            into("") // Place `pack.mcmeta` at the root of the ZIP
        }

        archiveFileName.set("mod-resources.zip")
        destinationDirectory.set(resourcePacksDir)

        doLast {
            val resultFile = File(destinationDirectory.get().asFile, archiveFileName.get())
            println("Resource pack created at: $resultFile")
        }
    }

    runClient {
        dependsOn(createResourcePack)
    }
}


