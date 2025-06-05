import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  application
  alias(libs.plugins.kotlinJvm) // this makes it a Kotlin-enabled project
  alias(libs.plugins.kotlinXSerialization)
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
  implementation("org.http4k:http4k-client-okhttp")
  implementation("org.http4k:http4k-config")
  implementation("org.http4k:http4k-core")
  implementation("org.http4k:http4k-format-kotlinx-serialization")
  implementation("org.http4k:http4k-ops-micrometer")
  implementation("org.http4k:http4k-security-oauth")
  implementation(fileTree(mapOf("dir" to "vendor", "include" to listOf("*.jar")))) // pre-release of Krouton

  // Kotlinx.html (our HTML templating eDSL)
  implementation(libs.kotlinxHtml) // the API
  implementation(libs.kotlinxHtmlJvm) // JVM implementation

  implementation("io.konform:konform:0.11.0")

  implementation("com.zaxxer:HikariCP:5.1.0")
  implementation("org.postgresql:postgresql:42.7.3")

  implementation("org.jdbi:jdbi3-core:3.49.4")
  implementation("org.jdbi:jdbi3-postgres:3.49.4") // jdbi plugin for postgres types
  implementation("org.jdbi:jdbi3-kotlin:3.49.4")
  implementation("org.jdbi:jdbi3-kotlin-sqlobject:3.49.4")
  implementation("org.jetbrains.kotlin:kotlin-reflect") // for jdbi-kotlin (hot queries should not use reflection to improve perf)

  implementation(platform("io.github.jan-tennert.supabase:bom:3.1.4"))
  implementation("io.ktor:ktor-client-java:3.1.3") // required by the supabase-kt package (can change to OkHttp when that gets used in other places)
  implementation("io.github.jan-tennert.supabase:auth-kt")
  implementation("com.auth0:java-jwt:4.5.0")
  // implementation("io.github.jan-tennert.supabase:postgrest-kt") we use SQL to query

  implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")

  implementation(libs.jacksonCore)
  implementation(libs.jacksonDatabind)
  implementation(libs.jacksonModuleKotlin) // Kotlin integrations for jackson
  implementation(libs.jacksonDatatypeJsr310) // for java8 time

  testImplementation("org.http4k:http4k-testing-approval")
  testImplementation("org.http4k:http4k-testing-hamkrest")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.0")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.12.0")
  testImplementation("org.junit.platform:junit-platform-launcher:1.12.2")
}
