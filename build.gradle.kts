plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.2"
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
    compileOnly("io.papermc.paper",                "paper-api",           project.properties["dependency_version_bukkit"].toString())
    compileOnly("net.md-5",                        "bungeecord-api",      project.properties["dependency_version_bungee"].toString())
    compileOnly("me.clip",                         "placeholderapi",      project.properties["dependency_version_placeholderapi"].toString())
    compileOnly("dev.lone",                        "api-itemsadder",      project.properties["dependency_version_itemsadder"].toString())
    compileOnly("net.luckperms",                   "api",                 project.properties["dependency_version_luckperms"].toString())
    compileOnly("org.apache.logging.log4j",        "log4j-core",          project.properties["dependency_version_log4j"].toString())
    compileOnly("me.libraryaddict.disguises",      "libsdisguises",       project.properties["dependency_version_libsdisguises"].toString())
    compileOnly("com.gitlab.ruany",                "LiteBansAPI",         project.properties["dependency_version_litebans"].toString())
    compileOnly("me.lucko",                        "spark-api",           project.properties["dependency_version_spark"].toString())
    compileOnly("com.github.MilkBowl",             "VaultAPI",            project.properties["dependency_version_vault"].toString())
    compileOnly("com.sk89q.worldguard",            "worldguard-bukkit",   project.properties["dependency_version_worldguard"].toString())
    compileOnly("org.slf4j",                       "slf4j-nop",           project.properties["dependency_version_slf4j"].toString())
    compileOnly("org.projectlombok",               "lombok",              project.properties["dependency_version_lombok"].toString())

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

    // Это платные плагины, у Вас их не будет.
    compileOnly(files("libs/CMI.jar"))
    compileOnly(files("libs/MobFarmManager.jar"))
    compileOnly(files("libs/TokenManager.jar"))
    compileOnly(files("libs/BotSentry.jar"))

    // Аннотации.
    annotationProcessor("com.velocitypowered",        "velocity-api",        project.properties["dependency_version_velocity"].toString())
    annotationProcessor("org.projectlombok",          "lombok",              project.properties["dependency_version_lombok"].toString())

    // Библиотеки, которые идут в .jar файл.
    implementation("org.apache.commons",              "commons-pool2",       project.properties["dependency_version_apache_commons"].toString())
    implementation("redis.clients",                   "jedis",               project.properties["dependency_version_redis"].toString())
    implementation("com.zaxxer",                      "HikariCP",            project.properties["dependency_version_hikaricp"].toString())
    implementation("com.google.code.gson",            "gson",                project.properties["dependency_version_gson"].toString())
    implementation("org.yaml",                        "snakeyaml",           project.properties["dependency_version_snakeyaml"].toString())
    implementation("com.mysql",                       "mysql-connector-j",   project.properties["dependency_version_mysql"].toString())
    implementation("org.mariadb.jdbc",                "mariadb-java-client", project.properties["dependency_version_mariadb"].toString())
    implementation("com.maxmind.geoip2",              "geoip2",              project.properties["dependency_version_geoip2"].toString())
    implementation("com.maxmind.db",                  "maxmind-db",          project.properties["dependency_version_maxmind"].toString())
    implementation("de.tr7zw",                        "item-nbt-api",        project.properties["dependency_version_itemnbt"].toString())
    implementation("dnsjava",                         "dnsjava",             project.properties["dependency_version_dns"].toString())
    implementation("org.gagravarr",                   "vorbis-java-core",    project.properties["dependency_version_vorbis"].toString())
}

val tokenizedJavaDir: Provider<Directory> = layout.buildDirectory.dir("generated/sources/tokenized/java")
val generateTokenizedJava by tasks.registering {
    val inDir = layout.projectDirectory.dir("src/main/java")
    val outDir = tokenizedJavaDir

    val props = project.properties
        .mapKeys { it.key }
        .mapValues { it.value?.toString() ?: "" }

    inputs.dir(inDir)
    inputs.properties(props)
    outputs.dir(outDir)

    doLast {
        val dollar = project.findProperty("dollar")?.toString() ?: "$"
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
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        archiveFileName.set(project.properties["lib_name"].toString() + ".jar")
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

        relocate("de.tr7zw.changeme.nbtapi", project.properties["lib_path"].toString() + ".shaded.nbt")
    }

    processResources {
        val props = project.properties
            .mapKeys { it.key }
            .mapValues { it.value?.toString() ?: "" }
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