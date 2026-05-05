package com.sneakynamegenerator

import org.bukkit.plugin.java.JavaPlugin
import com.sneakynamegenerator.commands.*
import com.sneakynamegenerator.generator.*
import org.bukkit.permissions.Permission
import java.io.InputStream

class SneakyNamegenerator : JavaPlugin() {

	companion object {
		const val IDENTIFIER = "sneakynamegenerator"
		lateinit var instance: SneakyNamegenerator
		lateinit var registry: GeneratorRegistry
		lateinit var generator: NameGenerator
		val sessionManager = SessionManager()

		public fun log(message: String) {
			instance.logger.info(message)
		}
	}

	/**
     * Initializes the plugin instance during server load.
     */
    override fun onLoad() {
        instance = this
    }
    
    override fun onEnable() {
        logger.info("SneakyNamegenerator plugin has been enabled!")

        // Save default config if it doesn't exist
        saveDefaultConfig()
        
        // Initialize generator
        reloadGenerator()

		// Register commands
        server.commandMap.register(IDENTIFIER, CommandNamegen(this))

		// Register permission nodes
		server.pluginManager.addPermission(Permission("${IDENTIFIER}.command.*"))
		server.pluginManager.addPermission(Permission("${IDENTIFIER}.command.reload"))
    }

    fun reloadGenerator() {
        try {
            val loader = ConfigLoader()
            val newRegistry = GeneratorRegistry()
            
            // Ensure data folder exists
            if (!dataFolder.exists()) {
                dataFolder.mkdirs()
            }

            // Extract default config and generators if missing
            val configFile = dataFolder.resolve("config.yml")
            if (!configFile.exists()) {
                saveResource("config.yml", false)
            }

            ensureGeneratorPack(
                "elven",
                listOf("components.yml", "templates.yml")
            )
            ensureGeneratorPack(
                "dwarven",
                listOf("components.yml", "templates.yml")
            )
            ensureGeneratorPack(
                "wildborne",
                listOf("components.yml", "templates.yml")
            )
            ensureGeneratorPack(
                "fae",
                listOf("components.yml", "templates.yml")
            )
            ensureGeneratorPack(
                "goblin",
                listOf("components.yml", "templates.yml")
            )
            ensureGeneratorPack(
                "kobold",
                listOf("components.yml", "templates.yml")
            )
            ensureGeneratorPack(
                "giant",
                listOf("components.yml", "templates.yml")
            )
            ensureGeneratorPack(
                "gnome",
                listOf("components.yml", "templates.yml")
            )
            ensureGeneratorPack(
                "halfling",
                listOf("components.yml", "templates.yml")
            )
            ensureGeneratorPack(
                "viking",
                listOf("components.yml", "templates.yml")
            )
            ensureGeneratorPack(
                "believer",
                listOf("templates.yml")
            )
            
            // Load all yml files recursively
            loader.loadFromDirectory(dataFolder, newRegistry)
            
            registry = newRegistry
            generator = NameGenerator(registry)
            logger.info("Successfully loaded name generator configuration from ${dataFolder.path}")
        } catch (e: Exception) {
            logger.severe("Failed to load name generator configuration: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun ensureGeneratorPack(packName: String, files: List<String>) {
        val packDir = dataFolder.resolve("generators/$packName")
        if (!packDir.exists()) packDir.mkdirs()

        for (file in files) {
            val target = packDir.resolve(file)
            if (!target.exists()) {
                saveResource("generators/$packName/$file", false)
            }
        }
    }
    
    override fun onDisable() {
        logger.info("SneakyNamegenerator plugin has been disabled!")
    }
    
}
