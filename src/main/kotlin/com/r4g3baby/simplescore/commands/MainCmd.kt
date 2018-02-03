package com.r4g3baby.simplescore.commands

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.commands.subcmds.Reload
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class MainCmd(plugin: SimpleScore) : CommandExecutor, TabExecutor {
    private val subCmds = ArrayList<SubCmd>()

    init {
        subCmds.add(Reload(plugin))
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            for (subSmd in subCmds) {
                if (subSmd.name.equals(args[0], true)) {
                    if (sender.hasPermission(subSmd.permission)) {
                        subSmd.run(sender, args.slice(IntRange(1, args.lastIndex)))
                        return true
                    }
                    break
                }
            }
            sender.sendMessage("Help goes here!")
        } else {
            sender.sendMessage("Help goes here!")
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): MutableList<String> {
        if (args.size == 1) {
            return subCmds.filter { it.name.equals(args[0], true) }.map { it.name }.toCollection(ArrayList())
        }
        return arrayListOf()
    }
}