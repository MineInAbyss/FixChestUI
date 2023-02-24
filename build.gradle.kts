import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("xyz.jpenilla.run-paper") version "2.0.1" // Adds runServer and runMojangMappedServer tasks for testing
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2" // Generates plugin.yml
    //id("io.papermc.paperweight.userdev") version "1.4.0" // NMS
}

group = "com.boy0000"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0-SNAPSHOT")

    //paperDevBundle("1.19.3-R0.1-SNAPSHOT") //NMS
    implementation(kotlin("stdlib-jdk8"))
}

tasks {

    /*assemble {
        dependsOn(reobfJar)
    }*/

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filteringCharset = Charsets.UTF_8.name()
    }

    runServer {
        minecraftVersion("1.19.3")
    }

    shadowJar {
        relocate("kotlin", "boy0000.shaded.kotlin")
        archiveFileName.set("FixChestUI.jar")
        //archiveFile.get().asFile.copyTo(layout.projectDirectory.file("run/plugins/ModernLightApi.jar").asFile, true)
    }

    build {
        dependsOn(shadowJar)
    }

    /*
    reobfJar {
      // This is an example of how you might change the output location for reobfJar. It's recommended not to do this
      // for a variety of reasons, however it's asked frequently enough that an example of how to do it is included here.
      outputJar.set(layout.buildDirectory.file("libs/PaperweightTestPlugin-${project.version}.jar"))
    }
     */
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}


bukkit {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "com.boy0000.fixchestui.FixChestUIPlugin"
    version = "${project.version}"
    apiVersion = "1.19"
    authors = listOf("Author")
    depend = listOf("ProtocolLib")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
