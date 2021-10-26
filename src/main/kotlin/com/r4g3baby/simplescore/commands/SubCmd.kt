package com.r4g3baby.simplescore.commands

import com.r4g3baby.simplescore.SimpleScore.Api.i18n
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

abstract class SubCmd(val name: String) {
    val description = i18n.t("cmd.$name.description", prefixed = false)
    val permission = "simplescore.cmd.$name"

    abstract fun run(sender: CommandSender, args: Array<out String>)

    open fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        return emptyList()
    }

    protected fun targetsFor(sender: CommandSender): List<String> {
        var players = Bukkit.getOnlinePlayers()
        if (sender is Player) {
            players = players.filter { sender != it && sender.canSee(it) }
        }
        return players.map { it.name }
    }
}