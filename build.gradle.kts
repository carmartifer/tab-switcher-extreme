import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

fun properties(key: String) = providers.gradleProperty(key)

plugins {
    // Java support
    id("java")
    // IntelliJ Platform Gradle Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
    id("org.jetbrains.intellij.platform")
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog")
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(properties("javaVersion").get())
    }
}

// Repositories are configured in settings.gradle.kts
dependencies {
    intellijPlatform {
        // Build against the lowest supported IntelliJ Platform version
        intellijIdea(properties("platformVersion"))
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups = listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security")
}

intellijPlatform {
    pluginConfiguration {
        // Keep the historical plugin ID so existing installations keep receiving updates
        id = "TabSwitchExtreme"
        name = properties("pluginName")
        version = properties("pluginVersion")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = properties("pluginSinceBuild")
            // No until-build: the plugin stays compatible with future IDE versions unless the verifier reports problems
            untilBuild = provider { null }
        }

        vendor {
            name = "Carlos M."
            email = "carmartifer@outlook.es"
            url = "https://github.com/carmartifer"
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }

    pluginVerification {
        ides {
            properties("verifierIdeVersions").get()
                .split(',')
                .map(String::trim)
                .filter(String::isNotEmpty)
                .forEach { create(IntelliJPlatformType.IntellijIdea, it) }
        }
    }
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    publishPlugin {
        dependsOn(patchChangelog)
    }
}
