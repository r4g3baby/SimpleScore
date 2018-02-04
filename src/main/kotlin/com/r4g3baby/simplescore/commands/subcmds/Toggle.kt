package com.r4g3baby.simplescore.commands.subcmds

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.commands.SubCmd
import org.bukkit.command.CommandSender

class Toggle(private val plugin: SimpleScore) : SubCmd("toggle", "simplescore.toggle") {
    override fun run(sender: CommandSender, args: List<String>) {
        sender.sendMessage(plugin.messagesConfig?.permission)
    }
}