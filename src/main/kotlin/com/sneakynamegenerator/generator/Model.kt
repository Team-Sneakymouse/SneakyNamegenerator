package com.sneakynamegenerator.generator

import java.util.Random
import java.util.regex.Pattern

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
    val hidden: Boolean = false,
    val maxLength: Int = 32
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

    private fun resolve(token: String, ctx: MutableMap<String, String>): String? {
        val cleanToken = token.removeSurrounding("%")
        
        // Try templates first
        templates[cleanToken]?.let { return expand(it, ctx, cleanToken) }
        
        // Then lists
        lists[cleanToken]?.let { return it.pick() }
        
        return null
    }

    private fun expand(template: NameTemplate, ctx: MutableMap<String, String>, templateName: String = "?"): String {
        val maxPickAttempts = 40
        var lastExpanded = ""
        repeat(maxPickAttempts) {
            val raw = template.variants.pick()
            var expanded = expandString(raw, ctx)

            template.cleanupPattern?.let { pattern ->
                expanded = expanded.replace(pattern.toRegex(), "")
            }

            template.capitalizationPattern?.let { capPattern ->
                expanded = applyCapitalization(expanded, capPattern)
            }

            lastExpanded = expanded
            if (expanded.length <= template.maxLength) return expanded
        }

        throw IllegalStateException(
            "Template '$templateName' exceeded maxLength ${template.maxLength} (last length ${lastExpanded.length}) after $maxPickAttempts variant picks."
        )
    }

    fun expandString(input: String, ctx: MutableMap<String, String> = mutableMapOf()): String {
        val regex = "%([^%]+)%".toRegex()
        var result = input
        
        // Resolve nested tokens
        while (regex.containsMatchIn(result)) {
            result = regex.replace(result) { match ->
                val tokenBody = match.groupValues[1]
                if (tokenBody.startsWith("=")) {
                    val expr = tokenBody.drop(1)
                    val colonIdx = expr.indexOf(':')
                    if (colonIdx >= 0) {
                        // Bind: %=var:token%
                        val rawVarName = expr.substring(0, colonIdx).trim()
                        val innerToken = expr.substring(colonIdx + 1).trim()
                        if (rawVarName.isEmpty() || innerToken.isEmpty()) {
                            match.value
                        } else {
                            val silent = rawVarName.endsWith("!")
                            val varName = if (silent) rawVarName.dropLast(1) else rawVarName
                            if (varName.isEmpty()) return@replace match.value

                            val resolved = resolve(innerToken, ctx)
                            if (resolved != null) {
                                ctx[varName] = resolved
                                if (silent) "" else resolved
                            } else {
                                match.value
                            }
                        }
                    } else {
                        // Reuse: %=var%
                        val varName = expr.trim()
                        ctx[varName] ?: match.value
                    }
                } else {
                    resolve(tokenBody, ctx) ?: match.value
                }
            }
        }
        
        return result
    }

    private fun applyCapitalization(name: String, patternString: String): String {
        return try {
            val pattern = Pattern.compile(patternString)
            val matcher = pattern.matcher(name)
            val sb = StringBuilder(name)
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                if (start < name.length) {
                    val charToReplace = name.substring(start, end)
                    sb.replace(start, end, charToReplace.uppercase())
                }
            }
            sb.toString()
        } catch (_: Exception) {
            name
        }
    }
}
