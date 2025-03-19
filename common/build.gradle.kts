plugins {
    id("io.freefair.lombok") version "8.13"
    id("com.gradleup.shadow")
}

val fabric_loader_version = properties["fabric_loader_version"] as String

architectury {
    common("fabric", "forge")
}

dependencies {
    val includeJar by configurations.register("includeJar")
    includeJar(files("../libs/WaifUPnP-1.2.0.jar"))

    modImplementation("net.fabricmc:fabric-loader:${fabric_loader_version}")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    configurations.implementation.configure{extendsFrom(configurations.getByName("includeJar"))}
}
