package com.r4g3baby.simplescore.commands.subcmds

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.commands.SubCmd
import org.bukkit.command.CommandSender

class Reload(private val plugin: SimpleScore) : SubCmd("reload", "simplescore.admin") {
    override fun run(sender: CommandSender, args: List<String>) {
        sender.sendMessage("Reloading")
        plugin.load()
        if (plugin.scoreboardManager!!.hasScoreboard(plugin.server.getWorld("world"))) {
            println(plugin.scoreboardManager!!.getScoreboard(plugin.server.getWorld("world")))
        }
        if (plugin.scoreboardManager!!.hasScoreboard(plugin.server.getWorld("world_the_end"))) {
            println(plugin.scoreboardManager!!.getScoreboard(plugin.server.getWorld("world_the_end")))
        }
        println(plugin.scoreboardManager!!.hasScoreboard(plugin.server.getWorld("world_nether")))
        sender.sendMessage("Done")
    }
}