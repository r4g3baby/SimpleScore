package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.models.ScoreLine
import com.r4g3baby.simplescore.scoreboard.models.ScoreboardWorld
import com.r4g3baby.simplescore.utils.configs.ConfigFile

class MainConfig(plugin: SimpleScore) : ConfigFile(plugin, "config") {
    val saveScoreboards = config.getBoolean("SaveScoreboards", true)
    val worlds: MutableMap<String, ScoreboardWorld> = HashMap()

    private val updateTime = config.getInt("UpdateTime", 20)
    init {
        if (config.isConfigurationSection("Worlds")) {
            val worldsSec = config.getConfigurationSection("Worlds")
            for (world in worldsSec.getKeys(false).filter { plugin.server.getWorld(it) != null && !worlds.containsKey(it) }) {
                if (worldsSec.isConfigurationSection(world)) {
                    val worldSec = worldsSec.getConfigurationSection(world)

                    val titles = ScoreLine()
                    (worldSec.get("Titles") as List<*>).forEach {
                        when (it) {
                            is String -> titles.add(it, updateTime)
                            is Map<*, *> -> titles.add(it["text"] as String, it["time"] as Int)
                            else -> {
                                plugin.logger.warning("Failed to parse titles expected String or Map but got ${it!!::class.java} instead.")
                            }
                        }
                    }

                    val scores = HashMap<Int, ScoreLine>()
                    if (worldSec.isConfigurationSection("Scores")) {
                        val scoresSec = worldSec.getConfigurationSection("Scores")
                        for (score in scoresSec.getKeys(false).filter { !scores.containsKey(it.toInt()) }) {
                            val scoreLine = ScoreLine()
                            (scoresSec.get(score) as List<*>).forEach {
                                when (it) {
                                    is String -> scoreLine.add(it, updateTime)
                                    is Map<*, *> -> scoreLine.add(it["text"] as String, it["time"] as Int)
                                    else -> {
                                        plugin.logger.warning("Failed to parse score \"$score\" expected String or Map but got ${it!!::class.java} instead.")
                                    }
                                }
                            }
                            scores[score.toInt()] = scoreLine
                        }
                    }
                    worlds[world] = ScoreboardWorld(titles, scores)
                }
            }
        }

        if (config.isConfigurationSection("Shared")) {
            val sharedWorlds = config.getConfigurationSection("Shared")
            for (shared in sharedWorlds.getKeys(false).filter { worlds.containsKey(it) }) {
                for (world in sharedWorlds.getStringList(shared).filter { plugin.server.getWorld(it) != null }) {
                    val original = worlds.getValue(shared)
                    worlds[world] = ScoreboardWorld(original.titles.clone(), original.scores.mapValues { entry -> entry.value.clone() })
                }
            }
        }
    }
}