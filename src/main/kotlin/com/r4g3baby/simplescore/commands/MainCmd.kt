package com.r4g3baby.simplescore.commands

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.commands.subcmds.Reload
import com.r4g3baby.simplescore.commands.subcmds.Toggle
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class MainCmd(private val plugin: SimpleScore) : CommandExecutor, TabExecutor {
    private val subCmds = listOf(
        Reload(plugin), Toggle(plugin)
    )

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            for (subSmd in subCmds) {
                if (subSmd.name.equals(args[0], true)) {
                    if (sender.hasPermission(subSmd.permission)) {
                        subSmd.run(sender, args.slice(IntRange(1, args.lastIndex)))
                    } else sender.sendMessage(plugin.messagesConfig.permission)
                    return true
                }
            }
            sender.sendMessage(plugin.messagesConfig.help)
        } else sender.sendMessage(plugin.messagesConfig.help)

        return true
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): MutableList<String> {
        return when {
            args.size == 1 -> {
                subCmds.filter {
                    it.name.startsWith(args[0], true) && sender.hasPermission(it.permission)
                }.map { it.name }.toCollection(ArrayList())
            }
            args.size > 1 -> {
                val subCmd = subCmds.firstOrNull { it.name.equals(args[0], true) }
                subCmd?.onTabComplete(sender, args.sliceArray(1 until args.size)) ?: mutableListOf()
            }
            else -> mutableListOf()
        }
    }
}