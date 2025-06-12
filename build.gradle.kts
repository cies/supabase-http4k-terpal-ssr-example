import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

//plugins {
//  application
//  alias(libs.plugins.kotlinJvm) // this makes it a Kotlin-enabled project
//  alias(libs.plugins.kotlinXSerialization)
//  id("com.google.devtools.ksp") version "2.1.0-1.0.29"
//}

plugins {
  kotlin("jvm") version "2.1.0" // this makes it a Kotlin-enabled project
  kotlin("plugin.serialization") version "2.1.0"
  id("io.exoquery.terpal-plugin") version "2.1.0-2.0.0.PL"
  id("com.google.devtools.ksp") version "2.1.0-1.0.29"
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
  implementation("org.http4k:http4k-ops-micrometer")
  implementation("org.http4k:http4k-client-okhttp")
  // implementation("org.http4k:http4k-security-oauth")

  // Kotlinx.html (our HTML templating eDSL)
  implementation(libs.kotlinxHtml) // the API
  implementation(libs.kotlinxHtmlJvm) // JVM implementation

  implementation("io.konform:konform:0.11.0")

  implementation("com.zaxxer:HikariCP:5.1.0")
  implementation("org.postgresql:postgresql:42.7.3")

//  implementation(platform("io.github.jan-tennert.supabase:bom:3.1.4"))
//  implementation("io.ktor:ktor-client-java:3.1.3") // required by the supabase-kt package (can change to OkHttp when that gets used in other places)
//  implementation("io.github.jan-tennert.supabase:auth-kt")
//  implementation("com.auth0:java-jwt:4.5.0") // TODO: remove
//  implementation("com.nimbusds:nimbus-jose-jwt:10.3")
  // implementation("io.github.jan-tennert.supabase:postgrest-kt") we use SQL to query

  implementation("dev.forkhandles:result4k:2.22.3.0")
  implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")

  implementation(libs.slf4jApi) // the facade API
  implementation(libs.log4jCore) // the core logging functionality
  implementation(libs.log4jSlf4j2Impl) // the connection to the facade
  implementation(libs.kotlinLogging) // a nice Kotlinesque wrapper

  api("io.exoquery:terpal-sql-jdbc:2.0.0.PL-1.2.0") {
    exclude(group = "org.xerial", module = "sqlite-jdbc") // 13MB we do not use (TODO: make issue in terpal-sql)
  }

  api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.1")
  api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
  implementation("com.squareup.moshi:moshi-kotlin:1.15.2") // still uses kotlin-reflect (try using `kotchi` instead to fix)
  ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")

  testImplementation("org.http4k:http4k-testing-approval")
  testImplementation("org.http4k:http4k-testing-hamkrest")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.0")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.12.0")
  testImplementation("org.junit.platform:junit-platform-launcher:1.12.2")
  testImplementation("org.assertj:assertj-core:3.26.3")
}
