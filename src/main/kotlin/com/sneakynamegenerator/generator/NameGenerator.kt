package com.sneakynamegenerator.generator

import java.util.regex.Pattern

class NameGenerator(val registry: GeneratorRegistry) {

    data class GenerationDebug(
        val type: String,
        val pickedVariant: String,
        val result: String,
        val attempts: Int,
    )

    /**
     * Generates a name of the specified type.
     */
    fun generate(type: String): String {
        return generateDebug(type).result
    }

    fun generateDebug(type: String): GenerationDebug {
        val template = registry.templates[type.lowercase()]
            ?: throw IllegalArgumentException("Unknown name type: $type")

        val maxAttempts = 40
        for (attempt in 1..maxAttempts) {
            val ctx = mutableMapOf<String, String>()
            val picked = template.variants.pick()
            var name = registry.expandString(picked, ctx)
            
            // Apply cleanup
            template.cleanupPattern?.let { pattern ->
                name = name.replace(pattern.toRegex(), "")
            }

            if (type.lowercase().startsWith("wildborne")) {
                name = softenLongConsonantRuns(name, maxConsonants = 3)
            }
            
            name = applyCapitalization(name, template.capitalizationPattern ?: "(?<=^|\\s).")
            
            if (name.length <= template.maxLength) {
                return GenerationDebug(
                    type = type,
                    pickedVariant = picked,
                    result = name,
                    attempts = attempt,
                )
            }
        }
        
        throw IllegalStateException("Failed to generate a name within max length of ${template.maxLength} for template '$type' after $maxAttempts attempts.")
    }

    /**
     * Trims runs of consonants longer than [maxConsonants] (helps wildborne syllable mashups).
     */
    private fun softenLongConsonantRuns(name: String, maxConsonants: Int): String {
        if (maxConsonants < 1) return name
        val vowels = setOf('a', 'e', 'i', 'o', 'u', 'y', 'A', 'E', 'I', 'O', 'U', 'Y')
        val sb = StringBuilder()
        var run = 0
        for (ch in name) {
            when {
                ch == ' ' || ch == '\'' || !ch.isLetter() -> {
                    run = 0
                    sb.append(ch)
                }
                ch in vowels -> {
                    run = 0
                    sb.append(ch)
                }
                else -> {
                    run++
                    if (run <= maxConsonants) sb.append(ch) else run = maxConsonants
                }
            }
        }
        return sb.toString()
    }

    private fun applyCapitalization(name: String, patternString: String): String {
        try {
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
            return sb.toString()
        } catch (e: Exception) {
            // Fallback to simple capitalization if regex fails
            return name.replaceFirstChar { it.uppercase() }
        }
    }
}
