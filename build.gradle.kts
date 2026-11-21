plugins {
    java
}

group = "su.samplecraft"
version = "1.0.0"
description = "All-in-one essentials suite for Paper 26.2-26.3, made by SampleCraft"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Provided by the server at runtime - never bundled into the jar.
    // 26.2.build.+ resolves to the newest stable Paper 26.2 build.
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
}

// Vault's economy interfaces are mirrored in stubs/src (compile-time only,
// the real classes come from VaultUnlocked at runtime), so no external repo is needed.
sourceSets {
    main {
        java.srcDir("stubs/src/main/java")
    }
}

java {
    // Paper 26.x requires Java 25 - the paper-api artifact itself is only
    // consumable by a JVM-25 toolchain, and the server runs on Java 25.
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
    options.compilerArgs.addAll(listOf("-Xlint:-deprecation", "-parameters"))
}

tasks.processResources {
    val props = mapOf("version" to project.version.toString())
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    archiveFileName.set("SamplesEssentials-${project.version}.jar")
    // Never ship the Vault compile stubs - the server's Vault plugin provides them.
    exclude("net/milkbowl/**")
    manifest {
        attributes(
            "Implementation-Title" to "SamplesEssentials",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "SampleCraft",
        )
    }
}

// `./gradlew build` -> build/libs/SamplesEssentials-1.0.0.jar
tasks.build {
    dependsOn(tasks.jar)
}
