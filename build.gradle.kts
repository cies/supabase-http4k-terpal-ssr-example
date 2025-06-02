import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  kotlin("jvm") version "2.1.21"
  application
  id("com.github.johnrengelman.shadow") version "8.1.1"
  id("gg.jte.gradle") version "3.2.1" // same as what `http4k-templates-jte` depends on
  id("app.cash.sqldelight") version "2.1.0"
}

buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }

  dependencies {
  }
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

application {
  mainClass = "com.example.ApplicationKt"
}

tasks {
  shadowJar {
    archiveBaseName.set(project.name)
    archiveClassifier = null
    archiveVersion = null
    mergeServiceFiles()
    dependsOn(distTar, distZip)
    isZip64 = true
  }
}

repositories {
  mavenCentral()
}

tasks {
  withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
      allWarningsAsErrors = false
      jvmTarget.set(JVM_21)
      freeCompilerArgs.add("-Xjvm-default=all")
    }
  }

  withType<Test> {
    useJUnitPlatform()
  }

  java {
    sourceCompatibility = VERSION_21
    targetCompatibility = VERSION_21
  }
}

dependencies {
  implementation(platform("org.http4k:http4k-bom:6.9.2.0"))
  implementation("org.http4k:http4k-client-okhttp")
  implementation("org.http4k:http4k-config")
  implementation("org.http4k:http4k-core")
  implementation("org.http4k:http4k-format-kotlinx-serialization")
  implementation("org.http4k:http4k-ops-micrometer")
  implementation("org.http4k:http4k-security-oauth")
  implementation("org.http4k:http4k-template-jte")
  implementation(fileTree(mapOf("dir" to "vendor", "include" to listOf("*.jar")))) // pre-release of Krouton

  jteGenerate("gg.jte:jte-models:3.2.1") // same as gradle plugin (see top of file)

  implementation("com.zaxxer:HikariCP:5.1.0")
  implementation("org.postgresql:postgresql:42.7.3")
  implementation("org.jdbi:jdbi3-core:3.49.4")
  implementation("org.jdbi:jdbi3-postgres:3.49.4") // jdbi plugin for postgres types
  implementation("org.jdbi:jdbi3-kotlin:3.49.4")
  implementation("org.jdbi:jdbi3-kotlin-sqlobject:3.49.4")
  implementation("org.jetbrains.kotlin:kotlin-reflect") // for jdbi-kotlin (hot queries should not use reflection to improve perf)

  testImplementation("org.http4k:http4k-testing-approval")
  testImplementation("org.http4k:http4k-testing-hamkrest")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.0")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.12.0")
  testImplementation("org.junit.platform:junit-platform-launcher:1.12.2")
}

jte {
  generate()
  binaryStaticContent.set(true)
  jteExtension("gg.jte.models.generator.ModelExtension")
}

sqldelight {
  databases {
    create("Database") {
      packageName.set("com.example")
      dialect("app.cash.sqldelight:postgresql-dialect:2.1.0")
      schemaOutputDirectory.set(file("src/main/sqldelight/databases"))
    }
  }
}
