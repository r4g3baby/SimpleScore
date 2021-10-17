package com.r4g3baby.simplescore.commands.subcmds

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.SimpleScore.Api.i18n
import com.r4g3baby.simplescore.commands.SubCmd
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Toggle : SubCmd("toggle") {
    private val otherPermission = "${this.permission}.other"

    override fun run(sender: CommandSender, args: Array<out String>) {
        if (sender is Player) {
            if (args.isNotEmpty()) {
                if (args[0].equals("on", true)) {
                    SimpleScore.manager.playersData.setHidden(SimpleScore.plugin, sender, false)
                    sender.sendMessage(i18n.t("cmd.toggle.shown"))
                } else if (args[0].equals("off", true)) {
                    SimpleScore.manager.playersData.setHidden(SimpleScore.plugin, sender, true)
                    sender.sendMessage(i18n.t("cmd.toggle.hidden"))
                } else if (sender.hasPermission(otherPermission)) {
                    targetOther(sender, args)
                } else sender.sendMessage(i18n.t("cmd.toggle.usage.player"))
            } else {
                if (SimpleScore.manager.playersData.toggleHidden(SimpleScore.plugin, sender)) {
                    sender.sendMessage(i18n.t("cmd.toggle.hidden"))
                } else sender.sendMessage(i18n.t("cmd.toggle.shown"))
            }
        } else {
            if (args.isNotEmpty()) {
                targetOther(sender, args)
            } else sender.sendMessage(i18n.t("cmd.toggle.usage.console"))
        }
    }

    private fun targetOther(sender: CommandSender, args: Array<out String>) {
        val target = Bukkit.getOnlinePlayers().find { it.name.equals(args[0], true) }
        if (target != null) {
            if (
                args.size == 1 || (args.size > 1 && (
                    args[1].equals("on", true) || args[1].equals("off", true)
                ))
            ) {
                run(target, args.sliceArray(1..args.lastIndex))
                if (SimpleScore.manager.playersData.isHiding(SimpleScore.plugin, target)) {
                    sender.sendMessage(i18n.t("cmd.toggle.other.hidden", target.name))
                } else sender.sendMessage(i18n.t("cmd.toggle.other.shown", target.name))
            } else {
                if (sender is Player) {
                    sender.sendMessage(i18n.t("cmd.toggle.usage.admin"))
                } else sender.sendMessage(i18n.t("cmd.toggle.usage.console"))
            }
        } else sender.sendMessage(i18n.t("cmd.notOnline"))
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        return when (args.size) {
            1 -> mutableListOf("on", "off").apply {
                if (sender !is Player) clear()

                if (sender.hasPermission(otherPermission)) {
                    var players = Bukkit.getOnlinePlayers()
                    if (sender is Player) {
                        players = players.filter { sender != it && sender.canSee(it) }
                    }
                    addAll(players.map { it.name })
                }
            }.filter { it.startsWith(args[0], true) }
            2 -> mutableListOf("on", "off").apply {
                if (args[0].equals("on", true) || args[0].equals("off", true)) clear()
                else if (!sender.hasPermission(otherPermission)) clear()
            }.filter { it.startsWith(args[1], true) }
            else -> emptyList()
        }
    }
}