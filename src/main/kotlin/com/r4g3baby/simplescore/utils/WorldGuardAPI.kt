package com.r4g3baby.simplescore.utils

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.codemc.worldguardwrapper.WorldGuardWrapper
import org.codemc.worldguardwrapper.flag.IWrappedFlag

object WorldGuardAPI {
    private lateinit var wrapper: WorldGuardWrapper
    private lateinit var scoreboardFlag: IWrappedFlag<Array<String>>

    fun init(plugin: Plugin): Boolean {
        if (plugin.server.pluginManager.getPlugin("WorldGuard") != null) {
            wrapper = WorldGuardWrapper.getInstance()

            var flag = wrapper.registerFlag("scoreboard", Array<String>::class.java, arrayOf())
            if (flag.isPresent) {
                scoreboardFlag = flag.get()
                return true
            } else {
                flag = wrapper.getFlag("scoreboard", Array<String>::class.java)
                if (flag.isPresent) {
                    scoreboardFlag = flag.get()
                    return true
                }
            }
        }
        return false
    }

    fun getFlag(player: Player, location: Location = player.location): Array<String>? {
        if (this::scoreboardFlag.isInitialized) {
            val flag = wrapper.queryFlag(player, location, scoreboardFlag)
            if (flag.isPresent) {
                return flag.get()
            }
        }
        return null
    }
}