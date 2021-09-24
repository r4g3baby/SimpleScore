package com.r4g3baby.simplescore.commands.subcmds

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.commands.SubCmd
import com.r4g3baby.simplescore.utils.updater.UpdateChecker
import org.bukkit.command.CommandSender

class Version : SubCmd("version") {
    override fun run(sender: CommandSender, args: Array<out String>) {
        val version = SimpleScore.plugin.description.version
        sender.sendMessage(SimpleScore.messages.checkingForUpdates.format(version))
        UpdateChecker(SimpleScore.plugin, 23243) { hasUpdate, newVersion ->
            if (hasUpdate) {
                sender.sendMessage(SimpleScore.messages.foundNewUpdate.format(newVersion))
            } else sender.sendMessage(SimpleScore.messages.runningLatest)
        }
    }
}