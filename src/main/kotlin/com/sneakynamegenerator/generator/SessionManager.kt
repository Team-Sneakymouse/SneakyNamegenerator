package com.sneakynamegenerator.generator

import java.util.*

class GeneratorSession(val type: String, val amount: Int) {
    val history = mutableListOf<List<String>>()
    var currentIndex = -1

    fun addSet(set: List<String>) {
        history.add(set)
        currentIndex = history.size - 1
    }

    fun hasNext(): Boolean = true // Can always generate more
    fun hasPrevious(): Boolean = currentIndex > 0

    fun next(): Int {
        currentIndex++
        return currentIndex
    }

    fun previous(): Int {
        if (hasPrevious()) {
            currentIndex--
        }
        return currentIndex
    }

    fun getCurrentSet(): List<String>? = history.getOrNull(currentIndex)
}

class SessionManager {
    private val sessions = mutableMapOf<UUID, GeneratorSession>()

    fun getSession(uuid: UUID): GeneratorSession? = sessions[uuid]

    fun createSession(uuid: UUID, type: String, amount: Int): GeneratorSession {
        val session = GeneratorSession(type, amount)
        sessions[uuid] = session
        return session
    }

    fun clearSession(uuid: UUID) {
        sessions.remove(uuid)
    }
}
