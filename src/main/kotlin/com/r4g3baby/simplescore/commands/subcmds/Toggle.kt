package com.r4g3baby.simplescore.commands.subcmds

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.commands.SubCmd
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Toggle(private val plugin: SimpleScore) : SubCmd("toggle", "simplescore.toggle") {
    override fun run(sender: CommandSender, args: List<String>) {
        if (args.isNotEmpty()) {
            if (sender.hasPermission("simplescore.toggle.other")) {
                @Suppress("DEPRECATION") val target = plugin.server.getPlayer(args[0])
                if (target != null) {
                    if (plugin.scoreboardManager.toggleScoreboard(target)) {
                        sender.sendMessage(plugin.messagesConfig.disabledOther.format(target.name))
                    } else sender.sendMessage(plugin.messagesConfig.enabledOther.format(target.name))
                } else sender.sendMessage(plugin.messagesConfig.notOnline)
            } else sender.sendMessage(plugin.messagesConfig.permission)
        } else {
            if (sender is Player) {
                if (plugin.scoreboardManager.toggleScoreboard(sender)) {
                    sender.sendMessage(plugin.messagesConfig.disabled)
                } else sender.sendMessage(plugin.messagesConfig.enabled)
            } else sender.sendMessage(plugin.messagesConfig.onlyPlayers)
        }
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): MutableList<String> {
        if (args.size == 1 && sender.hasPermission("simplescore.toggle.other")) {
            return plugin.server.onlinePlayers.map { it.name }.toMutableList()
        }
        return mutableListOf()
    }
}