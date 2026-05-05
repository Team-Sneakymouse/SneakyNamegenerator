package com.sneakynamegenerator.generator

import org.yaml.snakeyaml.Yaml
import java.io.InputStream

class ConfigLoader {
    private val yaml = Yaml()

    fun load(inputStream: InputStream, registry: GeneratorRegistry) {
        val data = try {
            yaml.load<Map<String, Any>>(inputStream)
        } catch (e: Exception) {
            null
        } ?: return

        // Load lists
        (data["lists"] as? Map<String, Any>)?.forEach { (name, content) ->
            val items = parseWeightedList(content)
            registry.registerList(name, WeightedList(items))
        }

        // Load templates
        (data["templates"] as? Map<String, Any>)?.forEach { (name, content) ->
            val template = parseTemplate(content)
            registry.registerTemplate(name, template)
        }
    }

    fun loadFromDirectory(directory: java.io.File, registry: GeneratorRegistry) {
        if (!directory.exists()) return
        if (directory.isFile) {
            if (directory.extension.lowercase() == "yml") {
                directory.inputStream().use { load(it, registry) }
            }
            return
        }

        directory.listFiles()?.forEach { file ->
            loadFromDirectory(file, registry)
        }
    }

    private fun parseWeightedList(content: Any): List<WeightedItem<String>> {
        return when (content) {
            is List<*> -> content.mapNotNull { item ->
                when (item) {
                    is Map<*, *> -> {
                        if (item.containsKey("value")) {
                            WeightedItem(
                                item["value"].toString(),
                                (item["weight"] as? Number)?.toDouble() ?: 1.0
                            )
                        } else {
                            // Handle format: - "some_value": 10
                            val firstEntry = item.entries.firstOrNull()
                            if (firstEntry != null) {
                                WeightedItem(
                                    firstEntry.key.toString(),
                                    (firstEntry.value as? Number)?.toDouble() ?: 1.0
                                )
                            } else null
                        }
                    }
                    else -> WeightedItem(item.toString(), 1.0)
                }
            }
            else -> emptyList()
        }
    }

    private fun parseTemplate(content: Any): NameTemplate {
        return when (content) {
            is List<*> -> {
                val variants = parseWeightedList(content)
                NameTemplate(WeightedList(variants))
            }
            is Map<*, *> -> {
                val rawVariants = content["pattern"] ?: content["variants"] ?: ""
                val variantsList = when (rawVariants) {
                    is List<*> -> parseWeightedList(rawVariants)
                    else -> listOf(WeightedItem(rawVariants.toString(), 1.0))
                }
                val cap = content["capitalization"]?.toString()
                val cleanup = content["cleanup"]?.toString()
                val hidden = content["hidden"] as? Boolean ?: false
                val maxLength = (content["maxLength"] as? Number)?.toInt() ?: 32
                NameTemplate(WeightedList(variantsList), cap, cleanup, hidden, maxLength)
            }
            else -> NameTemplate(WeightedList(listOf(WeightedItem(content.toString(), 1.0))))
        }
    }
}
