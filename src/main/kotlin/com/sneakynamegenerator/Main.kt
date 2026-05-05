package com.sneakynamegenerator

import com.sneakynamegenerator.generator.ConfigLoader
import com.sneakynamegenerator.generator.GeneratorRegistry
import com.sneakynamegenerator.generator.NameGenerator
import java.io.File

fun main(args: Array<out String>) {
    if (args.isEmpty()) {
        println("Usage: <config_path> <type> [count] [debug|--debug]")
        return
    }

    val configPath = args[0]
    val type = args.getOrNull(1) ?: "default"
    val count = args.getOrNull(2)?.toIntOrNull() ?: 1
    val debug = args.any { it.equals("debug", ignoreCase = true) || it.equals("--debug", ignoreCase = true) }

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

        println("=== template: $type")
        println("Generating $count names of type '$type'${if (debug) " (debug)" else ""}...")

        val rows = mutableListOf<Pair<String, String?>>()
        repeat(count) {
            try {
                if (debug) {
                    val res = generator.generateDebug(type)
                    rows.add(res.result to res.pickedVariant)
                } else {
                    rows.add(generator.generate(type) to null)
                }
            } catch (e: Exception) {
                rows.add("Error generating '$type': ${e.message}" to null)
            }
        }

        if (!debug) {
            for ((name, _) in rows) {
                println("- $name")
            }
        } else {
            val nameColWidth = (rows.maxOfOrNull { it.first.length } ?: 0).coerceAtLeast(20) + 2
            val variantHeader = "picked_variant/pattern"
            println("${"name".padEnd(nameColWidth)}$variantHeader")
            println("${"-".repeat("name".length).padEnd(nameColWidth)}${"-".repeat(variantHeader.length)}")
            for ((name, picked) in rows) {
                println("${name.padEnd(nameColWidth)}${picked ?: ""}")
            }
        }
    } catch (e: Exception) {
        println("Failed to load config: ${e.message}")
        e.printStackTrace()
    }
}
