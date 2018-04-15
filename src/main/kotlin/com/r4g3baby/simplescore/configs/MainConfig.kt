package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.models.ScoreboardWorld
import com.r4g3baby.simplescore.utils.configs.ConfigFile
import java.util.*
import kotlin.collections.HashMap

class MainConfig(plugin: SimpleScore) : ConfigFile(plugin, "config") {
    val updateTime = config.getInt("UpdateTime", 20)
    val worlds: MutableMap<String, ScoreboardWorld> = HashMap()
    val shared: MutableMap<String, List<String>> = HashMap()

    init {
        if (config.isConfigurationSection("Worlds")) {
            val worldsSec = config.getConfigurationSection("Worlds")
            for (world in worldsSec.getKeys(false).filter { plugin.server.getWorld(it) != null && !worlds.containsKey(it) }) {
                if (worldsSec.isConfigurationSection(world)) {
                    val worldSec = worldsSec.getConfigurationSection(world)
                    val titles = LinkedList(worldSec.getStringList("Titles"))
                    val scores = HashMap<Int, Queue<String>>()
                    if (worldSec.isConfigurationSection("Scores")) {
                        val scoresSec = worldSec.getConfigurationSection("Scores")
                        for (score in scoresSec.getKeys(false).filter { !scores.containsKey(it.toInt()) }) {
                            val scoreInt = score.toInt()
                            scores[scoreInt] = LinkedList(scoresSec.getStringList(score))
                        }
                    }
                    worlds[world] = ScoreboardWorld(titles, scores)
                }
            }
        }

        if (config.isConfigurationSection("Shared")) {
            val sharedWorlds = config.getConfigurationSection("Shared")
            for (world in sharedWorlds.getKeys(false).filter { worlds.containsKey(it) }) {
                shared[world] = sharedWorlds.getStringList(world).filter { plugin.server.getWorld(it) != null }
            }
        }
    }
}