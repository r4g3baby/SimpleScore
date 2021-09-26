package com.r4g3baby.simplescore.commands.subcmds

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.SimpleScore.Api.i18n
import com.r4g3baby.simplescore.commands.SubCmd
import com.r4g3baby.simplescore.utils.updater.UpdateChecker
import org.bukkit.command.CommandSender

class Version : SubCmd("version") {
    override fun run(sender: CommandSender, args: Array<out String>) {
        val version = SimpleScore.plugin.description.version
        sender.sendMessage(i18n.t("cmd.version.checking", version))
        UpdateChecker(SimpleScore.plugin, 23243) { hasUpdate, newVersion ->
            if (hasUpdate) {
                val downloadUrl = "https://www.spigotmc.org/resources/simplescore.23243/"
                sender.sendMessage(i18n.t("cmd.version.foundUpdate", newVersion, downloadUrl))
            } else sender.sendMessage(i18n.t("cmd.version.runningLatest"))
        }
    }
}