package com.sneakynamegenerator

import com.sneakynamegenerator.generator.ConfigLoader
import com.sneakynamegenerator.generator.GeneratorRegistry
import com.sneakynamegenerator.generator.NameGenerator
import java.io.File
import java.io.FileInputStream

fun main(args: Array<out String>) {
    if (args.isEmpty()) {
        println("Usage: <config_path> <type> [count]")
        return
    }

    val configPath = args[0]
    val type = args.getOrNull(1) ?: "default"
    val count = args.getOrNull(2)?.toIntOrNull() ?: 1

    val configFile = File(configPath)
    if (!configFile.exists()) {
        println("Config file not found: $configPath")
        return
    }

    try {
        val loader = ConfigLoader()
        val registry = GeneratorRegistry()
        loader.loadFromDirectory(configFile, registry)
        
        val generator = NameGenerator(registry)

        println("Generating $count names of type '$type'...")
        repeat(count) {
            try {
                println("- ${generator.generate(type)}")
            } catch (e: Exception) {
                println("Error generating '$type': ${e.message}")
            }
        }
    } catch (e: Exception) {
        println("Failed to load config: ${e.message}")
        e.printStackTrace()
    }
}
