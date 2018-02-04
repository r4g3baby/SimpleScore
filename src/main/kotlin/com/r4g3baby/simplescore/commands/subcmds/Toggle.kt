package com.r4g3baby.simplescore.commands.subcmds

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.commands.SubCmd
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Toggle(private val plugin: SimpleScore) : SubCmd("toggle", "simplescore.toggle") {
    override fun run(sender: CommandSender, args: List<String>) {
        if (sender is Player) {
            if (plugin.scoreboardManager != null) {
                if (plugin.scoreboardManager!!.toggleScoreboard(sender)) {
                    sender.sendMessage(plugin.messagesConfig?.disabled)
                } else {
                    sender.sendMessage(plugin.messagesConfig?.enabled)
                }
            }
        } else {
            sender.sendMessage(plugin.messagesConfig?.onlyPlayers)
        }
    }
}