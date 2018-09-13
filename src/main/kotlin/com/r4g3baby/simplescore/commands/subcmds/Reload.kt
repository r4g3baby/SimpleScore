package com.r4g3baby.simplescore.commands.subcmds

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.commands.SubCmd
import org.bukkit.command.CommandSender

class Reload(private val plugin: SimpleScore) : SubCmd("reload", "simplescore.reload") {
    override fun run(sender: CommandSender, args: List<String>) {
        sender.sendMessage(plugin.messagesConfig.reloading)
        plugin.load()
        sender.sendMessage(plugin.messagesConfig.reloaded)
    }
}