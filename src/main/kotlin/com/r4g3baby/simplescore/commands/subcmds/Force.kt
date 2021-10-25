package com.r4g3baby.simplescore.commands.subcmds

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.SimpleScore.Api.i18n
import com.r4g3baby.simplescore.commands.SubCmd
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Force : SubCmd("force") {
    private val otherPermission = "${this.permission}.other"

    override fun run(sender: CommandSender, args: Array<out String>) {
        if (sender is Player) {
            if (args.isNotEmpty()) {
                val scoreboard = SimpleScore.manager.scoreboards.get(args[0])
                if (args[0].equals("none", true)) {
                    SimpleScore.manager.playersData.setScoreboard(SimpleScore.plugin, sender, null)
                    sender.sendMessage(i18n.t("cmd.force.removed"))
                } else if (scoreboard != null && scoreboard.canSee(sender)) {
                    SimpleScore.manager.playersData.setScoreboard(SimpleScore.plugin, sender, scoreboard)
                    sender.sendMessage(i18n.t("cmd.force.changed", scoreboard.name))
                } else if (sender.hasPermission(otherPermission)) {
                    targetOther(sender, args)
                } else sender.sendMessage(i18n.t("cmd.force.notFound", args[0]))
            } else {
                if (sender.hasPermission(otherPermission)) {
                    sender.sendMessage(i18n.t("cmd.force.usage.admin"))
                } else sender.sendMessage(i18n.t("cmd.force.usage.player"))
            }
        } else {
            if (args.isNotEmpty()) {
                targetOther(sender, args)
            } else sender.sendMessage(i18n.t("cmd.force.usage.console"))
        }
    }

    private fun targetOther(sender: CommandSender, args: Array<out String>) {
        if (args.size > 1) {
            val target = Bukkit.getOnlinePlayers().find { it.name.equals(args[0], true) }
            if (target != null) {
                val scoreboard = SimpleScore.manager.scoreboards.get(args[1])
                if (args[1].equals("none", true)) {
                    SimpleScore.manager.playersData.setScoreboard(SimpleScore.plugin, target, null)
                    sender.sendMessage(i18n.t("cmd.force.other.removed", target.name))
                } else if (scoreboard != null && scoreboard.canSee(target)) {
                    SimpleScore.manager.playersData.setScoreboard(SimpleScore.plugin, target, scoreboard)
                    sender.sendMessage(i18n.t("cmd.force.other.changed", target.name, scoreboard.name))
                } else sender.sendMessage(i18n.t("cmd.force.notFound", args[1]))
            } else sender.sendMessage(i18n.t("cmd.notOnline"))
        } else {
            if (sender is Player) {
                sender.sendMessage(i18n.t("cmd.force.usage.admin"))
            } else sender.sendMessage(i18n.t("cmd.force.usage.console"))
        }
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        return when (args.size) {
            1 -> mutableListOf("none").apply {
                if (sender is Player) {
                    addAll(
                        SimpleScore.manager.scoreboards.filter {
                            it.value.canSee(sender)
                        }.map { it.value.name }
                    )
                } else clear()

                if (sender.hasPermission(otherPermission)) addAll(targetsFor(sender))
            }.filter { it.startsWith(args[0], true) }
            2 -> mutableListOf("none").apply {
                if (!sender.hasPermission(otherPermission)) clear()
                else {
                    val target = Bukkit.getOnlinePlayers().find { it.name.equals(args[0], true) }
                    if (target != null) {
                        addAll(
                            SimpleScore.manager.scoreboards.filter {
                                it.value.canSee(target)
                            }.map { it.value.name }
                        )
                    } else clear()
                }
            }.filter { it.startsWith(args[1], true) }
            else -> emptyList()
        }
    }
}