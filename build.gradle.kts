import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  alias(libs.plugins.kotlinJvm) // this makes it a Kotlin-enabled project
  alias(libs.plugins.kotlinXSerialization) // running this compiler plugin prevents the need for `kotlin-reflect`

  // This is not specified in the version catalog as the version is derived off `kotlinVersion`.
  // For updates see: https://plugins.gradle.org/plugin/io.exoquery.terpal-plugin
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
  mainClass = "AppKt"
}

repositories {
  mavenCentral()
}

// Got tired of the src/main/kotlin/com/example path prefix.
// Since this is not a library, I don't care for the convention.
sourceSets {
  main {
    kotlin {
      srcDir("src")
    }
    resources.srcDirs("src/resources")
  }
  test {
    kotlin {
      srcDir("test")
    }
  }
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

  // JSON encoding/decoding, our one-and-only. Also used by `terpal-sql` and our custom form handling logic.
  api(libs.kotlinxSerializationCore)
  api(libs.kotlinxSerializationJson)

  implementation(libs.result4k) // a nice Result type (basically `Either` in Haskell)

  implementation(libs.hikariCp)
  implementation(libs.postgresql)
  api(libs.terpalSqlJdbc) {
    exclude(group = "org.xerial", module = "sqlite-jdbc") // 13MB we do not use (TODO: make issue in terpal-sql)
  }

  // Kotlinx.html (our HTML templating eDSL)
  implementation(libs.kotlinxHtml) // the API
  implementation(libs.kotlinxHtmlJvm) // JVM implementation

  implementation(libs.konform) // for validation

  implementation(libs.kotlinLogging) // a nice Kotlinesque wrapper
  implementation(libs.slf4jApi) // the facade API
  implementation(libs.slf4jSimple) // a super simple logging implementation (log4j-core is nearly 2MB)


  // Test dependencies
  testImplementation(libs.http4kTestingApproval)
  testImplementation(libs.http4kTestingHamkrest)
  testImplementation(libs.junitJupiterApi)
  testImplementation(libs.junitJupiterEngine)
  testImplementation(libs.junitPlatformLauncher)
  testImplementation(libs.assertj)
}
