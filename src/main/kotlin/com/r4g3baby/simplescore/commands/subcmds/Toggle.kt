package com.r4g3baby.simplescore.commands.subcmds

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.SimpleScore.Api.i18n
import com.r4g3baby.simplescore.commands.SubCmd
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Toggle : SubCmd("toggle") {
    override fun run(sender: CommandSender, args: Array<out String>) {
        if (args.isNotEmpty()) {
            if (sender.hasPermission("${this.permission}.other")) {
                val target = Bukkit.getOnlinePlayers().find { it.name.equals(args[0], true) }
                if (target != null) {
                    if (SimpleScore.manager.playersData.toggleForceHidden(target)) {
                        sender.sendMessage(i18n.t("cmd.toggle.other.hidden", target.name))
                    } else sender.sendMessage(i18n.t("cmd.toggle.other.shown", target.name))
                } else sender.sendMessage(i18n.t("cmd.notOnline"))
            } else sender.sendMessage(i18n.t("cmd.noPermission"))
        } else {
            if (sender is Player) {
                if (SimpleScore.manager.playersData.toggleForceHidden(sender)) {
                    sender.sendMessage(i18n.t("cmd.toggle.hidden"))
                } else sender.sendMessage(i18n.t("cmd.toggle.shown"))
            } else sender.sendMessage(i18n.t("cmd.onlyPlayers"))
        }
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): MutableList<String> {
        if (args.size == 1 && sender.hasPermission("${this.permission}.other")) {
            return Bukkit.getOnlinePlayers().map { it.name }.toMutableList()
        }
        return mutableListOf()
    }
}