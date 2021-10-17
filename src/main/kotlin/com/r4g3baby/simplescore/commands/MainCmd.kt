package com.r4g3baby.simplescore.commands

import com.r4g3baby.simplescore.SimpleScore.Api.i18n
import com.r4g3baby.simplescore.commands.subcmds.Reload
import com.r4g3baby.simplescore.commands.subcmds.Toggle
import com.r4g3baby.simplescore.commands.subcmds.Version
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class MainCmd : CommandExecutor, TabExecutor {
    private val subCmds = listOf(
        Reload(), Toggle(), Version()
    )

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            for (subSmd in subCmds) {
                if (subSmd.name.equals(args[0], true)) {
                    if (sender.hasPermission(subSmd.permission)) {
                        subSmd.run(sender, args.sliceArray(1..args.lastIndex))
                    } else sender.sendMessage(i18n.t("cmd.noPermission"))
                    return true
                }
            }
            sender.sendMessage(i18n.t("cmd.help.show", prefixed = false))
        } else sender.sendMessage(i18n.t("cmd.help.show", prefixed = false))

        return true
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): List<String> {
        return when {
            args.size == 1 -> {
                subCmds.filter {
                    it.name.startsWith(args[0], true) && sender.hasPermission(it.permission)
                }.map { it.name }.toMutableList()
            }
            args.size > 1 -> {
                val subCmd = subCmds.firstOrNull { it.name.equals(args[0], true) }
                subCmd?.onTabComplete(sender, args.sliceArray(1..args.lastIndex)) ?: emptyList()
            }
            else -> emptyList()
        }
    }
}