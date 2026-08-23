package com.r4g3baby.simplescore.bukkit.command.subcmd

import com.r4g3baby.simplescore.BukkitPlugin
import com.r4g3baby.simplescore.ProjectInfo
import com.r4g3baby.simplescore.bukkit.command.SubCmd
import com.r4g3baby.simplescore.core.util.checkForUpdates
import com.r4g3baby.simplescore.core.util.translateColorCodes
import org.bukkit.command.CommandSender

class Version(private val plugin: BukkitPlugin) : SubCmd(plugin, "version") {
    override fun run(sender: CommandSender, args: Array<out String>) {
        if (plugin.manager.config.checkForUpdates) {
            sender.sendMessage(plugin.i18n.t("cmd.version.checking", ProjectInfo.VERSION))
            plugin.scheduler.runTaskAsync {
                checkForUpdates({ newVersion ->
                    sender.sendMessage(plugin.i18n.t("cmd.version.newVersion", newVersion))
                    sender.sendMessage(translateColorCodes("&7${ProjectInfo.DOWNLOAD_URL}"))
                }, { ex ->
                    if (ex == null) {
                        sender.sendMessage(plugin.i18n.t("cmd.version.latest"))
                    } else sender.sendMessage(plugin.i18n.t("cmd.version.failed"))
                })
            }
        } else sender.sendMessage(plugin.i18n.t("cmd.version.current", ProjectInfo.VERSION))
    }
}
