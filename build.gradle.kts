@file:Suppress("VulnerableLibrariesLocal", "UnstableApiUsage")

import com.github.spotbugs.snom.SpotBugsTask
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension

plugins {
    id("java")
    id("checkstyle")
    id("com.github.spotbugs")
    id("com.gradleup.shadow")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.fabricmc.net")                                         // Fabric.
    maven("https://repo.papermc.io/repository/maven-public/")                   // Paper.
    maven("https://maven.enginehub.org/repo/")                                  // WorldGuard.
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")     // Bukkit. (Если Paper недоступен)
    maven("https://oss.sonatype.org/content/repositories/snapshots/")           // Bungee.
    maven("https://jitpack.io/")                                                // GitHub.
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI.
    maven("https://mvn.lib.co.nz/public")                                       // LibsDisguises.
    maven("https://repo.dmulloy2.net/repository/public/")                       // ProtocolLib.
    maven("https://repo1.maven.org/maven2/")                                    // LuckPerms.
    maven("https://maven.devs.beer/")                                           // ItemsAdder.
    maven("https://repo.codemc.io/repository/maven-public/")                    // NBT-API.
    maven("https://repo.plasmoverse.com/releases")                              // PlasmoVoice.
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${providers.gradleProperty("dependency_version_bukkit").get()}")
    compileOnly("net.md-5:bungeecord-api:${providers.gradleProperty("dependency_version_bungee").get()}")
    compileOnly("me.clip:placeholderapi:${providers.gradleProperty("dependency_version_placeholderapi").get()}")
    compileOnly("dev.lone:api-itemsadder:${providers.gradleProperty("dependency_version_itemsadder").get()}")
    compileOnly("net.luckperms:api:${providers.gradleProperty("dependency_version_luckperms").get()}")
    compileOnly("org.apache.logging.log4j:log4j-core:${providers.gradleProperty("dependency_version_log4j").get()}")
    compileOnly("me.libraryaddict.disguises:libsdisguises:${providers.gradleProperty("dependency_version_libsdisguises").get()}")
    compileOnly("com.gitlab.ruany:LiteBansAPI:${providers.gradleProperty("dependency_version_litebans").get()}")
    compileOnly("me.lucko:spark-api:${providers.gradleProperty("dependency_version_spark").get()}")
    compileOnly("com.github.MilkBowl:VaultAPI:${providers.gradleProperty("dependency_version_vault").get()}")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:${providers.gradleProperty("dependency_version_worldguard").get()}")
    compileOnly("org.slf4j:slf4j-nop:${providers.gradleProperty("dependency_version_slf4j").get()}")
    compileOnly("org.projectlombok:lombok:${providers.gradleProperty("dependency_version_lombok").get()}")

    // Плагины, которые легче импортировать вручную, чем пытаться подгрузить их репозиторий.
    compileOnly(files("libs/Velocity.jar"))
    compileOnly(files("libs/FastAsyncWorldEdit.jar"))
    compileOnly(files("libs/UltimateTimber.jar"))
    compileOnly(files("libs/GadgetsMenu.jar"))
    compileOnly(files("libs/CMILib.jar"))
    compileOnly(files("libs/ajParkour.jar"))
    compileOnly(files("libs/TAB.jar"))
    compileOnly(files("libs/emotecraft.jar"))
    compileOnly(files("libs/ProtocolLib.jar"))
    compileOnly(files("libs/ItemJoin.jar"))
    compileOnly(files("libs/TokenManager.jar"))

    // Это слитые платные плагины с сайтов по типу black-minecraft.
    // Не используйте их для собственной безопасности, они нужны ТОЛЬКО для компиляции проекта.
    compileOnly(files("libs/CMI-LEAKED.jar"))
    compileOnly(files("libs/MobFarmManager-LEAKED.jar"))
    compileOnly(files("libs/BotSentry-LEAKED.jar"))
    compileOnly(files("libs/ForceResourcepacks-LEAKED.jar"))
    compileOnly(files("libs/Nexo-LEAKED.jar"))

    // Аннотации.
    annotationProcessor("com.velocitypowered:velocity-api:${providers.gradleProperty("dependency_version_velocity").get()}")
    annotationProcessor("org.projectlombok:lombok:${providers.gradleProperty("dependency_version_lombok").get()}")

    // Библиотеки, которые идут в .jar файл.
    implementation("org.apache.commons:commons-pool2:${providers.gradleProperty("dependency_version_apache_commons").get()}")
    implementation("redis.clients:jedis:${providers.gradleProperty("dependency_version_redis").get()}")
    implementation("com.zaxxer:HikariCP:${providers.gradleProperty("dependency_version_hikaricp").get()}")
    implementation("com.google.code.gson:gson:${providers.gradleProperty("dependency_version_gson").get()}")
    implementation("org.yaml:snakeyaml:${providers.gradleProperty("dependency_version_snakeyaml").get()}")
    implementation("com.mysql:mysql-connector-j:${providers.gradleProperty("dependency_version_mysql").get()}")
    implementation("org.mariadb.jdbc:mariadb-java-client:${providers.gradleProperty("dependency_version_mariadb").get()}")
    implementation("com.maxmind.geoip2:geoip2:${providers.gradleProperty("dependency_version_geoip2").get()}")
    implementation("com.maxmind.db:maxmind-db:${providers.gradleProperty("dependency_version_maxmind").get()}")
    implementation("de.tr7zw:item-nbt-api:${providers.gradleProperty("dependency_version_itemnbt").get()}")
    implementation("dnsjava:dnsjava:${providers.gradleProperty("dependency_version_dns").get()}")
    implementation("org.gagravarr:vorbis-java-core:${providers.gradleProperty("dependency_version_vorbis").get()}")
}

extensions.configure<CheckstyleExtension> {
	toolVersion = providers.gradleProperty("gradle_version_checkstyle").get()
	config = rootProject.resources.text.fromFile(rootProject.file("config/checkstyle/checkstyle.xml"))
	maxErrors = 0
	maxWarnings = 0
}

tasks.withType<Checkstyle>().configureEach {
	javaLauncher.set(javaToolchains.launcherFor {
		languageVersion.set(JavaLanguageVersion.of(21))
	})
}

val spotbugsAuxiliary = configurations.create("spotbugsAuxiliary") {
	isCanBeConsumed = false
	isCanBeResolved = true
}
dependencies.add(spotbugsAuxiliary.name, "org.jetbrains.kotlin:kotlin-stdlib:${providers.gradleProperty("dependency_version_kotlin").get()}@jar")

val spotbugsReport = layout.buildDirectory.file("reports/spotbugs/main/spotbugs.html")
tasks.withType<SpotBugsTask>().matching { it.name == "spotbugsMain" }.configureEach {
	excludeFilter.set(rootProject.layout.projectDirectory.file("config/spotbugs/suppressions.xml"))
	reports.create("html") {
		required.set(true)
		outputLocation.set(spotbugsReport)
		setStylesheet("fancy-hist.xsl")
	}
}
pluginManager.withPlugin("java") {
	tasks.named<SpotBugsTask>("spotbugsMain") {
		auxClassPaths.from(spotbugsAuxiliary)
	}
}


val tokenizedJavaDir: Provider<Directory> = layout.buildDirectory.dir("generated/sources/tokenized/java")
val generateTokenizedJava = tasks.register("generateTokenizedJava") {
    description = "Замена переменных"
    val inDir = layout.projectDirectory.dir("src/main/java")
    val outDir = tokenizedJavaDir

    val props = providers.gradlePropertiesPrefixedBy("").get()

    inputs.dir(inDir)
    inputs.properties(props)
    outputs.dir(outDir)

    doLast {
        val dollar = providers.gradleProperty("dollar").get()
        val inRoot = inDir.asFile
        val outRoot = outDir.get().asFile

        if (outRoot.exists()) outRoot.deleteRecursively()
        outRoot.mkdirs()

        inRoot.walkTopDown().forEach { f ->
            if (!f.isFile) return@forEach
            if (!f.name.endsWith(".java")) return@forEach

            val rel = f.relativeTo(inRoot).path
            val target = File(outRoot, rel)
            target.parentFile.mkdirs()

            var text = f.readText(Charsets.UTF_8)
            for ((k, v) in props) {
                text = text.replace(dollar + "{${k}}", v)
            }
            target.writeText(text, Charsets.UTF_8)
        }
    }
}

tasks {
    shadowJar {
        isZip64 = true
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set(project.providers.gradleProperty("lib_name").get() + ".jar")
        destinationDirectory.set(file("$rootDir/result/"))

        dependencies {
            include(dependency("redis.clients:.*"))
            include(dependency("org.apache.commons:.*"))
            include(dependency("com.zaxxer:.*"))
            include(dependency("com.google.code.gson:.*"))
            include(dependency("org.yaml:.*"))
            include(dependency("com.mysql:.*"))
            include(dependency("org.mariadb.jdbc:.*"))
            include(dependency("com.maxmind.geoip2:.*"))
            include(dependency("com.maxmind.db:.*"))
            include(dependency("de.tr7zw:.*"))
            include(dependency("dnsjava:.*"))
            include(dependency("org.gagravarr:.*"))
        }

        relocate("de.tr7zw.changeme.nbtapi", project.providers.gradleProperty("lib_path").get() + ".shaded.nbt")
    }

    processResources {
        val props = providers.gradlePropertiesPrefixedBy("").get()

        inputs.properties(props)
        expand(props)
    }

    named<JavaCompile>("compileJava") {
        dependsOn(generateTokenizedJava)
        source = fileTree(tokenizedJavaDir)
    }

    build {
        dependsOn(shadowJar)
    }
}