package com.r4g3baby.simplescore.commands.subcmds

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.commands.SubCmd
import org.bukkit.command.CommandSender

class Reload : SubCmd("reload") {
    override fun run(sender: CommandSender, args: Array<out String>) {
        sender.sendMessage(SimpleScore.messages.reloading)
        SimpleScore.reload()
        sender.sendMessage(SimpleScore.messages.reloaded)
    }
}