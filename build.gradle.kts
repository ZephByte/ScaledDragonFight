import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.0"
    id("fabric-loom") version "1.15-SNAPSHOT"
    id("maven-publish")
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

version = "${project.property("mod_version")}+${stonecutter.current.version}"
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

val obfuscated = stonecutter.eval(stonecutter.current.version, "<26")
val targetJavaVersion = if (obfuscated) 21 else 25
java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("scaleddragonfight") {
            sourceSet("main")
            sourceSet("client")
        }
    }
}


repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    if (obfuscated) {
        mappings(loom.officialMojangMappings())
    } else {
        // 26.x ships unobfuscated; intermediary 0.0.0 is an identity passthrough that satisfies Loom.
        mappings("net.fabricmc:intermediary:0.0.0:v2")
        // The remapping plugin leaves Mixin off the compile classpath for unobfuscated versions.
        implementation("net.fabricmc:sponge-mixin:${project.property("mixin_version")}")
    }
    // Obfuscated versions need mod dependencies remapped to Mojang names.
    val modDependency = if (obfuscated) "modImplementation" else "implementation"
    modDependency("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modDependency("net.fabricmc:fabric-language-kotlin:${project.property("kotlin_loader_version")}")
    modDependency("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    // Config (bundled into the mod jar via jar-in-jar)
    implementation("com.electronwill.night-config:toml:${project.property("night_config_version")}")
    include("com.electronwill.night-config:toml:${project.property("night_config_version")}")
    include("com.electronwill.night-config:core:${project.property("night_config_version")}")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))
    inputs.property("minecraft_dependency", project.property("minecraft_dependency"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version") as String,
            "minecraft_dependency" to project.property("minecraft_dependency") as String,
            "loader_version" to project.property("loader_version") as String,
            "kotlin_loader_version" to project.property("kotlin_loader_version") as String
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

// Dev servers run on the Java version real servers use; older loaders can't read Java 25 classes.
tasks.withType<net.fabricmc.loom.task.AbstractRunTask>().configureEach {
    javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(targetJavaVersion)) })
}

// The intermediary passthrough has no "named" namespace, so sources can only be remapped for obfuscated versions.
tasks.remapSourcesJar {
    enabled = obfuscated
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName}" }
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}

val modVersion = project.version as String
val gameVersionList = (project.property("modrinth_versions") as String).split(",").map { it.trim() }

publishMods {
    file.set(tasks.remapJar.flatMap { it.archiveFile })
    version.set(modVersion)
    changelog.set(providers.environmentVariable("CHANGELOG").orElse(""))
    type.set(
        when {
            modVersion.contains("-alpha") -> ALPHA
            modVersion.contains("-beta")  -> BETA
            else                          -> STABLE
        }
    )
    modLoaders.add("fabric")

    modrinth {
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN").orElse(""))
        projectId.set(providers.gradleProperty("modrinth_id").orElse(""))
        minecraftVersions.addAll(gameVersionList)
        requires { slug.set("fabric-api") }
        requires { slug.set("fabric-language-kotlin") }
    }
}
