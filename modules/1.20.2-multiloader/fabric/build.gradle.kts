plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
    platformSetupLoomIde()
    fabric()
}

val minecraftVersion = project.properties["minecraft_version"] as String

configurations {
    create("common")
    create("shadowCommon")
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    getByName("developmentFabric").extendsFrom(configurations["common"])
}

loom {
    accessWidenerPath.set(project(":${project.properties["project_path"]}:common").loom.accessWidenerPath)

    // Fabric Datagen Gradle config. Remove if not using Fabric datagen
    runs.create("datagen") {
        server()
        name("Data Generation")
        vmArg("-Dfabric-api.datagen")
        vmArg("-Dfabric-api.datagen.output-dir=${project(":${project.properties["project_path"]}:common").file("src/main/generated/resources").absolutePath}")
        vmArg("-Dfabric-api.datagen.modid=examplemod")

        runDir("build/datagen")
    }
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${project.properties["fabric_loader_version"]}")
    modApi("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_api_version"]}+$minecraftVersion")


    modCompileOnly("com.terraformersmc:modmenu:${project.properties["modmenu_version"]}")
	modRuntimeOnly("com.terraformersmc:modmenu:${project.properties["modmenu_version"]}")

    "common"(project(":${project.properties["project_path"]}:common", "namedElements")) { isTransitive = false }
    "shadowCommon"(project(":${project.properties["project_path"]}:common", "transformProductionFabric")) { isTransitive = false }
}

tasks {
    base.archivesName.set(base.archivesName.get() + "-Fabric")
    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }

    shadowJar {
        configurations = listOf(project.configurations.getByName("shadowCommon"))
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        injectAccessWidener.set(true)
        inputFile.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)
    }

    jar.get().archiveClassifier.set("dev")

    sourcesJar {
        val commonSources = project(":${project.properties["project_path"]}:common").tasks.sourcesJar
        dependsOn(commonSources)
        from(commonSources.get().archiveFile.map { zipTree(it) })
    }
}

components {
    java.run {
        if (this is AdhocComponentWithVariants)
            withVariantsFromConfiguration(project.configurations.shadowRuntimeElements.get()) { skip() }
    }
}

repositories {
    // ModMenu
    maven("https://maven.terraformersmc.com/")
}

publishing {
    publications.create<MavenPublication>("mavenCommon") {
        artifactId = "${project.properties["archives_base_name"]}" + "-Fabric"
        from(components["java"])
    }

    repositories {
        mavenLocal()
        maven {
            val releasesRepoUrl = "https://example.com/releases"
            val snapshotsRepoUrl = "https://example.com/snapshots"
            url = uri(if (project.version.toString().endsWith("SNAPSHOT") || project.version.toString().startsWith("0")) snapshotsRepoUrl else releasesRepoUrl)
            name = "ExampleRepo"
            credentials {
                username = project.properties["repoLogin"]?.toString()
                password = project.properties["repoPassword"]?.toString()
            }
        }
    }
}