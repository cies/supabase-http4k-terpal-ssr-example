import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  alias(libs.plugins.kotlinJvm) // this makes it a Kotlin-enabled project
  alias(libs.plugins.kotlinXSerialization)
  id("io.exoquery.terpal-plugin") version "${libs.versions.kotlinVersion.get()}-2.0.0.PL"
  application
}

buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

application {
  mainClass = "com.example.appKt"
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
  implementation(platform(libs.http4kBom))
  implementation("org.http4k:http4k-core")
  implementation("org.http4k:http4k-config")
  implementation("org.http4k:http4k-format-kotlinx-serialization")
  implementation("org.http4k:http4k-client-okhttp")

  api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.1")
  api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

  implementation("dev.forkhandles:result4k:2.22.3.0")

  implementation("com.zaxxer:HikariCP:5.1.0")
  implementation("org.postgresql:postgresql:42.7.3")
  api("io.exoquery:terpal-sql-jdbc:2.0.0.PL-1.2.0") {
    exclude(group = "org.xerial", module = "sqlite-jdbc") // 13MB we do not use (TODO: make issue in terpal-sql)
  }

  // Kotlinx.html (our HTML templating eDSL)
  implementation(libs.kotlinxHtml) // the API
  implementation(libs.kotlinxHtmlJvm) // JVM implementation
  implementation("io.konform:konform:0.11.0")

  implementation(libs.kotlinLogging) // a nice Kotlinesque wrapper
  implementation(libs.slf4jApi) // the facade API
  implementation(libs.slf4jSimple) // a super simple logging implementation (log4j-core is nearly 2MB)


  testImplementation("org.http4k:http4k-testing-approval")
  testImplementation("org.http4k:http4k-testing-hamkrest")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.0")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.12.0")
  testImplementation("org.junit.platform:junit-platform-launcher:1.12.2")
  testImplementation("org.assertj:assertj-core:3.26.3")
}

tasks.register("depsize") {
  description = "Prints dependencies for \"default\" configuration"
  doLast {
    listConfigurationDependencies(configurations["default"])
  }
}

tasks.register("depsize-all-configurations") {
  description = "Prints dependencies for all available configurations"
  doLast {
    configurations
      .filter { it.isCanBeResolved }
      .forEach { listConfigurationDependencies(it) }
  }
}

fun listConfigurationDependencies(configuration: Configuration) {
  val formatStr = "%,10.2f"
  val size = configuration.sumOf { it.length() / (1024.0 * 1024.0) }
  val out = StringBuffer()
  out.append("\nConfiguration name: \"${configuration.name}\"\n")
  if (size > 0) {
    out.append("Total dependencies size:".padEnd(65))
    out.append("${String.format(formatStr, size)} Mb\n\n")

    configuration.sortedBy { -it.length() }.forEach {
      out.append(it.name.padEnd(65))
      out.append("${String.format(formatStr, (it.length() / 1024.0))} kb\n")
    }
  } else {
    out.append("No dependencies found")
  }
  println(out)
}
