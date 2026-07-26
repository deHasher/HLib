pluginManagement {
	val shadowPluginVersion = providers.gradleProperty("gradle_version_shadow").get()
	val spotbugsPluginVersion = providers.gradleProperty("gradle_version_spotbugs").get()

	plugins {
		id("com.gradleup.shadow") version shadowPluginVersion
		id("com.github.spotbugs") version spotbugsPluginVersion
	}

	repositories {
		gradlePluginPortal()
		mavenCentral()
		mavenLocal()
	}
}