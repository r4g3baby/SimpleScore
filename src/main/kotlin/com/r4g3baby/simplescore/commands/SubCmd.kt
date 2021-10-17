package com.r4g3baby.simplescore.commands

import org.bukkit.command.CommandSender

abstract class SubCmd(val name: String) {
    val permission = "simplescore.$name"

    abstract fun run(sender: CommandSender, args: Array<out String>)

    open fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        return emptyList()
    }
}