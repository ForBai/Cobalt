import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm")
  id("fabric-loom")
  `maven-publish`
  java
}

val baseGroup: String by project
val lwjglVersion: String by project
val modVersion: String by project
val modName: String by project

version = modVersion
group = baseGroup

base {
  archivesName = modName
}

val docVersionsDir = projectDir.resolve("docs-versions")
val currentVersion = version.toString()
val currentVersionDir = docVersionsDir.resolve(currentVersion)

repositories {
  mavenCentral()
  maven("https://maven.meteordev.org/releases")
  maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

dependencies {
  minecraft("com.mojang:minecraft:${property("minecraft_version")}")
  mappings(loom.officialMojangMappings())

  modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
  modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
  modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

  modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")
  runtimeOnly("org.apache.httpcomponents:httpclient:4.5.14")

  modImplementation("org.lwjgl:lwjgl-nanovg:${lwjglVersion}")
  include("org.lwjgl:lwjgl-nanovg:${lwjglVersion}")

  listOf("windows", "linux", "macos", "macos-arm64").forEach {
    modImplementation("org.lwjgl:lwjgl-nanovg:${lwjglVersion}:natives-$it")
    include("org.lwjgl:lwjgl-nanovg:${lwjglVersion}:natives-$it")
  }

  implementation("meteordevelopment:discord-ipc:1.1")
  include("meteordevelopment:discord-ipc:1.1")

  implementation("org.reflections:reflections:0.10.2")
  include("org.reflections:reflections:0.10.2")
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      artifact(tasks.named("remapJar")) {
        builtBy(tasks.named("remapJar"))
      }

      artifact(tasks.named("kotlinSourcesJar")) {
        builtBy(tasks.named("remapSourcesJar"))
      }

      pom {
        name.set(modName)
        description.set("Hypixel Skyblock QoL mod")
        url.set("https://github.com/CobaltScripts/Cobalt")
      }
    }
  }

  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/CobaltScripts/Cobalt")
      credentials {
        username = System.getenv("GITHUB_ACTOR")
        password = System.getenv("GITHUB_TOKEN")
      }
    }
  }
}

tasks {
  processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
      expand(getProperties())
      expand(mutableMapOf("version" to project.version))
    }
  }

  compileKotlin {
    compilerOptions {
      jvmTarget = JvmTarget.JVM_21
    }
  }
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}
