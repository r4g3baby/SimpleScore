package com.r4g3baby.simplescore.commands

import org.bukkit.command.CommandSender

abstract class SubCmd(val name: String, val permission: String) {
    abstract fun run(sender: CommandSender, args: List<String>)

    open fun onTabComplete(sender: CommandSender, args: Array<out String>): MutableList<String> {
        return mutableListOf()
    }
}