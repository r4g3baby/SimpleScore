package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.models.ScoreLine
import com.r4g3baby.simplescore.scoreboard.models.ScoreboardWorld
import com.r4g3baby.simplescore.utils.configs.ConfigFile

class MainConfig(plugin: SimpleScore) : ConfigFile(plugin, "config") {
    val saveScoreboards = config.getBoolean("SaveScoreboards", true)
    val worlds = HashMap<String, ScoreboardWorld>()

    private val updateTime = config.getInt("UpdateTime", 20)

    init {
        if (config.isConfigurationSection("Worlds")) {
            val worldsSec = config.getConfigurationSection("Worlds")
            for (world in worldsSec.getKeys(false).filter { !worlds.containsKey(it.toLowerCase()) }) {
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
                    worlds[world.toLowerCase()] = ScoreboardWorld(titles, scores)
                }
            }
        }

        if (config.isConfigurationSection("Shared")) {
            val sharedWorlds = config.getConfigurationSection("Shared")
            for (shared in sharedWorlds.getKeys(false).filter { worlds.containsKey(it.toLowerCase()) }) {
                for (world in sharedWorlds.getStringList(shared).filter { !worlds.containsKey(it.toLowerCase()) }) {
                    val original = worlds.getValue(shared.toLowerCase())
                    worlds[world.toLowerCase()] = ScoreboardWorld(original.titles.clone(), original.scores.mapValues { entry -> entry.value.clone() })
                }
            }
        }
    }
}