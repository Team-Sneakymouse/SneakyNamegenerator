package com.sneakynamegenerator

import org.bukkit.plugin.java.JavaPlugin
import com.sneakynamegenerator.commands.*
import com.sneakynamegenerator.generator.*
import java.io.InputStream

class SneakyNamegenerator : JavaPlugin() {

	companion object {
		const val IDENTIFIER = "sneakynamegenerator"
		lateinit var instance: SneakyNamegenerator
		lateinit var registry: GeneratorRegistry
		lateinit var generator: NameGenerator

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
    }

    fun reloadGenerator() {
        try {
            val loader = ConfigLoader()
            val newRegistry = GeneratorRegistry()
            
            // Ensure data folder exists
            if (!dataFolder.exists()) {
                dataFolder.mkdirs()
                saveDefaultConfig()
            }
            
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
    
    override fun onDisable() {
        logger.info("SneakyNamegenerator plugin has been disabled!")
    }
    
}
