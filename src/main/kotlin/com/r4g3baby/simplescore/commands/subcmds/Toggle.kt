package com.r4g3baby.simplescore.commands.subcmds

import com.r4g3baby.simplescore.SimpleScore
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
                    if (SimpleScore.scoreboardManager.toggleScoreboard(target)) {
                        sender.sendMessage(SimpleScore.messages.disabledOther.format(target.name))
                    } else sender.sendMessage(SimpleScore.messages.enabledOther.format(target.name))
                } else sender.sendMessage(SimpleScore.messages.notOnline)
            } else sender.sendMessage(SimpleScore.messages.permission)
        } else {
            if (sender is Player) {
                if (SimpleScore.scoreboardManager.toggleScoreboard(sender)) {
                    sender.sendMessage(SimpleScore.messages.disabled)
                } else sender.sendMessage(SimpleScore.messages.enabled)
            } else sender.sendMessage(SimpleScore.messages.onlyPlayers)
        }
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): MutableList<String> {
        if (args.size == 1 && sender.hasPermission("${this.permission}.other")) {
            return Bukkit.getOnlinePlayers().map { it.name }.toMutableList()
        }
        return mutableListOf()
    }
}