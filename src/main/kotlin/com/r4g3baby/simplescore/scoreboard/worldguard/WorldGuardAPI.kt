package com.r4g3baby.simplescore.scoreboard.worldguard

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.codemc.worldguardwrapper.WorldGuardWrapper
import org.codemc.worldguardwrapper.flag.IWrappedFlag

object WorldGuardAPI {
    private lateinit var wrapper: WorldGuardWrapper
    private lateinit var scoreboardFlag: IWrappedFlag<String>

    val isEnabled get() = this::scoreboardFlag.isInitialized

    internal fun init() {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            wrapper = WorldGuardWrapper.getInstance()

            var flag = wrapper.registerFlag("scoreboard", String::class.java, "")
            if (flag.isPresent) {
                scoreboardFlag = flag.get()
            } else {
                flag = wrapper.getFlag("scoreboard", String::class.java)
                if (flag.isPresent) {
                    scoreboardFlag = flag.get()
                }
            }
        }
    }

    fun getFlag(player: Player, location: Location = player.location): List<String> {
        if (isEnabled) {
            val flag = wrapper.queryFlag(player, location, scoreboardFlag)
            if (flag.isPresent) {
                return flag.get().split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
        }
        return emptyList()
    }
}