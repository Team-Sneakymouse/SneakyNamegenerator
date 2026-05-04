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
        if (sender !is Player) {
            sender.sendMessage("Only players can use this command.")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("Please provide a type for the name.")
            return false
        }

        val type = args[0].lowercase()

        return true
    }
}