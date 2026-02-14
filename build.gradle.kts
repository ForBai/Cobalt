import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin)
  alias(libs.plugins.loom)
  `maven-publish`
}

val baseGroup: String by project
val modVersion: String by project
val modName: String by project

version = modVersion
group = baseGroup

base {
  archivesName = modName
}

repositories {
  mavenCentral()
  maven("https://maven.meteordev.org/releases")
  maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

dependencies {
  minecraft(libs.minecraft)
  mappings(loom.officialMojangMappings())
  modImplementation(libs.bundles.fabric)

  implementation(libs.nanovg) { include(this) }
  implementation(libs.bundles.included) { include(this) }

  listOf("windows", "linux", "macos", "macos-arm64").forEach {
    implementation(variantOf(libs.nanovg) { classifier("natives-$it") }) {
      include(this)
    }
  }

  runtimeOnly(libs.httpclient)
  modRuntimeOnly(libs.devauth)
}

tasks {
  processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
      expand(getProperties())
      expand(mutableMapOf("version" to project.version))
    }
  }

  publishing {
    publications {
      create<MavenPublication>("mavenJava") {
        artifact(remapJar) {
          builtBy(remapJar)
        }

        artifact(kotlinSourcesJar) {
          builtBy(remapSourcesJar)
        }
      }
    }
  }

  compileKotlin {
    compilerOptions {
      jvmTarget = JvmTarget.JVM_21
    }
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}
