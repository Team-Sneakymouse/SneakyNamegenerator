package com.sneakynamegenerator

import org.bukkit.plugin.java.JavaPlugin
import com.sneakynamegenerator.commands.*

class SneakyNamegenerator : JavaPlugin() {

	companion object {
		const val IDENTIFIER = "sneakynamegenerator"
		lateinit var instance: SneakyNamegenerator

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

		// Register commands
        //
        
        // Save default config if it doesn't exist
        saveDefaultConfig()
    }
    
    override fun onDisable() {
        logger.info("SneakyNamegenerator plugin has been disabled!")
    }
    
}
