plugins {
    java
}

group = "su.samplecraft"
version = "1.0.0"
description = "All-in-one essentials suite for Paper 26.2-26.3, made by SampleCraft"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    // Vault API (VaultUnlocked source build; the Vault jar itself is not on Maven Central)
    maven("https://jitpack.io")
}

dependencies {
    // Provided by the server at runtime - never bundled into the jar.
    // 26.2.build.+ resolves to the newest stable Paper 26.2 build.
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    // Economy API used by the vault bridge and premium RTP (provided by VaultUnlocked on the server).
    compileOnly("com.github.TheNewEconomy:Vault:2.20.2")
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
