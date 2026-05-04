package com.sneakynamegenerator.generator

import java.util.regex.Pattern

class NameGenerator(val registry: GeneratorRegistry) {

    /**
     * Generates a name of the specified type.
     */
    fun generate(type: String): String {
        val template = registry.templates[type.lowercase()]
            ?: throw IllegalArgumentException("Unknown name type: $type")

        val rawName = registry.expandString(template.variants.pick())
        
        return applyCapitalization(rawName, template.capitalizationPattern ?: "(?<=^|\\W).")
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
