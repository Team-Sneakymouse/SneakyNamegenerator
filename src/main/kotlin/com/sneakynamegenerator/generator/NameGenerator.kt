package com.sneakynamegenerator.generator

import java.util.regex.Pattern

class NameGenerator(val registry: GeneratorRegistry) {

    /**
     * Generates a name of the specified type.
     */
    fun generate(type: String): String {
        val template = registry.templates[type.lowercase()]
            ?: throw IllegalArgumentException("Unknown name type: $type")

        val maxAttempts = 10
        for (attempt in 1..maxAttempts) {
            var name = registry.expandString(template.variants.pick())
            
            // Apply cleanup
            template.cleanupPattern?.let { pattern ->
                name = name.replace(pattern.toRegex(), "")
            }
            
            name = applyCapitalization(name, template.capitalizationPattern ?: "(?<=^|\\s).")
            
            if (name.length <= template.maxLength) {
                return name
            }
        }
        
        throw IllegalStateException("Failed to generate a name within max length of ${template.maxLength} for template '$type' after 10 attempts.")
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
