package com.sneakynamegenerator.commands

import com.sneakynamegenerator.SneakyNamegenerator
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextColor
import com.sneakynamegenerator.generator.GeneratorSession

/**
 * Command for generating a name.
 * Usage: /namegen [type]
 */
class CommandNamegen(private val plugin: SneakyNamegenerator) : CommandBase("namegen") {
    init {
        this.usageMessage = "/${this@CommandNamegen.name} <type|reload|next|prev> [amount]"
        this.description = "Generates a name."
    }

    override fun execute(
        sender: CommandSender,
        commandLabel: String,
        args: Array<out String>,
    ): Boolean {
        if (!sender.hasPermission(this.permission!!)) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED))
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage(Component.text("Usage: /namegen <type|reload|next|prev> [amount]", NamedTextColor.YELLOW))
            return true
        }

        val action = args[0].lowercase()

        if (action == "reload") {
            if (!sender.hasPermission("${SneakyNamegenerator.IDENTIFIER}.command.reload")) {
                sender.sendMessage(Component.text("You do not have permission to reload the configuration.", NamedTextColor.RED))
                return true
            }
            plugin.reloadGenerator()
            sender.sendMessage(Component.text("Name generator configuration reloaded!", NamedTextColor.GREEN))
            return true
        }

        if (sender !is Player) {
            sender.sendMessage(Component.text("Only players can use name generation sessions.", NamedTextColor.RED))
            return true
        }

        val player = sender
        val sessionManager = SneakyNamegenerator.sessionManager

        when (action) {
            "next" -> {
                val session = sessionManager.getSession(player.uniqueId)
                if (session == null) {
                    player.sendMessage(Component.text("No active session. Start one with /namegen <type>", NamedTextColor.RED))
                    return true
                }
                
                // If we are currently viewing history, we might want to just go forward in history
                if (session.currentIndex < session.history.size - 1) {
                    session.next()
                } else {
                    // Generate new set
                    try {
                        val set = List(session.amount) { SneakyNamegenerator.generator.generate(session.type) }
                        session.addSet(set)
                    } catch (e: Exception) {
                        player.sendMessage(Component.text("Error generating names: ${e.message}", NamedTextColor.RED))
                        return true
                    }
                }
                displaySet(player, session)
            }
            "prev" -> {
                val session = sessionManager.getSession(player.uniqueId)
                if (session == null || !session.hasPrevious()) {
                    player.sendMessage(Component.text("No previous results to show.", NamedTextColor.RED))
                    return true
                }
                session.previous()
                displaySet(player, session)
            }
            else -> {
                // New session
                val amount = args.getOrNull(1)?.toIntOrNull() ?: plugin.config.getInt("default_amount", 8)
                try {
                    val session = sessionManager.createSession(player.uniqueId, action, amount)
                    val set = List(amount) { SneakyNamegenerator.generator.generate(action) }
                    session.addSet(set)
                    displaySet(player, session)
                } catch (e: IllegalArgumentException) {
                    player.sendMessage(Component.text("Unknown name type: $action", NamedTextColor.RED))
                } catch (e: Exception) {
                    player.sendMessage(Component.text("Error: ${e.message}", NamedTextColor.RED))
                }
            }
        }

        return true
    }

    private fun displaySet(player: Player, session: GeneratorSession) {
        val set = session.getCurrentSet() ?: return
        
        val header = Component.text()
            .append(Component.text("Generated Names ", NamedTextColor.YELLOW))
            .append(Component.text("(", NamedTextColor.GRAY))
            .append(Component.text(session.type, NamedTextColor.AQUA))
            .append(Component.text(")", NamedTextColor.GRAY))
            .append(Component.text(" - Page ${session.currentIndex + 1}", NamedTextColor.DARK_GRAY))
            .build()
        
        player.sendMessage(Component.empty())
        player.sendMessage(header)
        
        for (name in set) {
            val nameComp = Component.text()
                .append(Component.text(" • ", NamedTextColor.GOLD))
                .append(Component.text(name, NamedTextColor.WHITE)
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy to clipboard", NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.copyToClipboard(name))
                )
                .build()
            player.sendMessage(nameComp)
        }
        
        val footer = Component.text()
        
        if (session.hasPrevious()) {
            footer.append(Component.text("[ < Previous ]", NamedTextColor.AQUA)
                .hoverEvent(HoverEvent.showText(Component.text("Show last set", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.runCommand("/namegen prev")))
        } else {
            footer.append(Component.text("[ < Previous ]", NamedTextColor.GRAY))
        }
        
        footer.append(Component.text("   "))
        
        footer.append(Component.text("[ Next > ]", NamedTextColor.AQUA)
            .hoverEvent(HoverEvent.showText(Component.text("Generate more", NamedTextColor.GRAY)))
            .clickEvent(ClickEvent.runCommand("/namegen next")))
            
        player.sendMessage(Component.empty())
        player.sendMessage(footer.build())
    }

    override fun tabComplete(
        sender: CommandSender,
        alias: String,
        args: Array<out String>
    ): List<String> {
        if (args.size == 1) {
            val options = mutableListOf("reload", "next", "prev")
            val templates = SneakyNamegenerator.registry.templates
                .filter { !it.value.hidden && !it.key.startsWith("_") }
                .keys
            options.addAll(templates)
            return options.filter { it.startsWith(args[0], ignoreCase = true) }
        }
        if (args.size == 2 && !args[0].equals("reload", true) && !args[0].equals("next", true) && !args[0].equals("prev", true)) {
            return listOf("1", "5", "8", "10", "15", "20")
        }
        return emptyList()
    }
}