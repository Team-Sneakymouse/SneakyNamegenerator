package com.sneakynamegenerator.commands

import com.sneakynamegenerator.SneakyNamegenerator
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Command for generating a name.
 * Usage: /namegen [type]
 */
class CommandNamegen(private val plugin: SneakyNamegenerator) : CommandBase("namegen") {
    init {
        this.usageMessage = "/${this@CommandNamegen.name} [type]"
        this.description = "Generates a name."
    }

    /**
     * Executes the /namegen command.
     *
     * @param sender The sender of the command
     * @param commandLabel The label used to invoke the command
     * @param args The command arguments
     * @return true if the command was handled successfully
     */
    override fun execute(
        sender: CommandSender,
        commandLabel: String,
        args: Array<out String>,
    ): Boolean {
        if (!sender.hasPermission(this.permission!!)) {
            sender.sendMessage("§cYou do not have permission to use this command.")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("§cUsage: /namegen <type|reload>")
            return true
        }

        val action = args[0].lowercase()

        if (action == "reload") {
            if (!sender.hasPermission("${SneakyNamegenerator.IDENTIFIER}.command.reload")) {
                sender.sendMessage("§cYou do not have permission to reload the configuration.")
                return true
            }
            plugin.reloadGenerator()
            sender.sendMessage("§aName generator configuration reloaded!")
            return true
        }

        try {
            val name = SneakyNamegenerator.generator.generate(action)
            sender.sendMessage("§7Generated name (§f$action§7): §b$name")
        } catch (e: Exception) {
            sender.sendMessage("§cError: ${e.message}")
        }

        return true
    }

    override fun tabComplete(
        sender: CommandSender,
        alias: String,
        args: Array<out String>
    ): List<String> {
        if (args.size == 1) {
            val options = mutableListOf("reload")
            options.addAll(SneakyNamegenerator.registry.templates.keys)
            return options.filter { it.startsWith(args[0], ignoreCase = true) }
        }
        return emptyList()
    }
}