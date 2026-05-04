package com.sneakynamegenerator.generator

import java.util.Random

class WeightedList<T>(private val items: List<WeightedItem<T>>) {
    private val totalWeight: Double = items.sumOf { it.weight }
    private val random = Random()

    fun pick(): T {
        if (items.isEmpty()) throw IllegalStateException("Cannot pick from an empty list")
        var r = random.nextDouble() * totalWeight
        for (item in items) {
            r -= item.weight
            if (r <= 0) return item.value
        }
        return items.last().value
    }
}

data class WeightedItem<T>(val value: T, val weight: Double = 1.0)

data class NameTemplate(
    val variants: WeightedList<String>,
    val capitalizationPattern: String? = null,
    val cleanupPattern: String? = null,
    val hidden: Boolean = false
)

class GeneratorRegistry {
    val lists = mutableMapOf<String, WeightedList<String>>()
    val templates = mutableMapOf<String, NameTemplate>()

    fun registerList(name: String, list: WeightedList<String>) {
        lists[name] = list
    }

    fun registerTemplate(name: String, template: NameTemplate) {
        templates[name] = template
    }

    fun resolve(token: String): String? {
        val cleanToken = token.removeSurrounding("%")
        
        // Try templates first
        templates[cleanToken]?.let { return expand(it) }
        
        // Then lists
        lists[cleanToken]?.let { return it.pick() }
        
        return null
    }

    private fun expand(template: NameTemplate): String {
        val raw = template.variants.pick()
        return expandString(raw)
    }

    fun expandString(input: String): String {
        val regex = "%([^%]+)%".toRegex()
        var result = input
        
        // Resolve nested tokens
        while (regex.containsMatchIn(result)) {
            result = regex.replace(result) { match ->
                resolve(match.groupValues[1]) ?: match.value
            }
        }
        
        return result
    }
}
