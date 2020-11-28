package com.r4g3baby.simplescore.commands.subcmds

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.commands.SubCmd
import com.r4g3baby.simplescore.utils.updater.UpdateChecker
import org.bukkit.command.CommandSender

class Version(private val plugin: SimpleScore) : SubCmd("version") {
    override fun run(sender: CommandSender, args: Array<out String>) {
        val version = plugin.description.version
        sender.sendMessage(plugin.messagesConfig.checkingForUpdates.format(version))
        UpdateChecker(plugin, 23243) { new, latest ->
            if (new) {
                sender.sendMessage(plugin.messagesConfig.foundNewUpdate.format(latest))
            } else sender.sendMessage(plugin.messagesConfig.runningLatest)
        }
    }
}