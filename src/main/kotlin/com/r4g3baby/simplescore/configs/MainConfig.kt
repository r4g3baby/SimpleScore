package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.models.ScoreLine
import com.r4g3baby.simplescore.scoreboard.models.Scoreboard
import com.r4g3baby.simplescore.utils.configs.ConfigFile

class MainConfig(plugin: SimpleScore) : ConfigFile(plugin, "config") {
    private val updateTime = config.getInt("UpdateTime", 20)
    val saveScoreboards = config.getBoolean("SaveScoreboards", true)

    val scoreboards = HashMap<String, Scoreboard>()
    val worlds = HashMap<String, String>()

    init {
        // Compatibility with older config format
        if (config.isConfigurationSection("Worlds") && !config.isConfigurationSection("Scoreboards")) {
            config.createSection("Scoreboards", config.getConfigurationSection("Worlds").getValues(true))
            config.set("Worlds", null)
            config.save(this)
        }

        if (config.isConfigurationSection("Shared") && !config.isConfigurationSection("Worlds")) {
            val sharedSec = config.getConfigurationSection("Shared")
            val worldsSec = config.createSection("Worlds")

            if (config.isConfigurationSection("Scoreboards")) {
                for (scoreboard in config.getConfigurationSection("Scoreboards").getKeys(false)) {
                    worldsSec.set(scoreboard, scoreboard)
                }
            }

            for (shared in sharedSec.getKeys(false)) {
                for (world in sharedSec.getStringList(shared)) {
                    worldsSec.set(world, shared)
                }
            }

            config.set("Shared", null)
            config.save(this)
        }

        if (config.isConfigurationSection("Scoreboards")) {
            val scoreboardsSec = config.getConfigurationSection("Scoreboards")
            for (scoreboard in scoreboardsSec.getKeys(false).filter { !scoreboards.containsKey(it) }) {
                if (scoreboardsSec.isConfigurationSection(scoreboard)) {
                    val scoreboardSec = scoreboardsSec.getConfigurationSection(scoreboard)

                    val titles = ScoreLine()
                    (scoreboardSec.get("Titles") as List<*>).forEach {
                        when (it) {
                            is String -> titles.add(it, updateTime)
                            is Map<*, *> -> titles.add(it["text"] as String, it["time"] as Int)
                            else -> {
                                plugin.logger.warning("Failed to parse titles expected String or Map but got ${it!!::class.java} instead.")
                            }
                        }
                    }

                    val scores = HashMap<Int, ScoreLine>()
                    if (scoreboardSec.isConfigurationSection("Scores")) {
                        val scoresSec = scoreboardSec.getConfigurationSection("Scores")
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
                    scoreboards[scoreboard] = Scoreboard(titles, scores)
                }
            }
        }

        if (config.isConfigurationSection("Worlds")) {
            val worldsSec = config.getConfigurationSection("Worlds")
            for (world in worldsSec.getKeys(false)) {
                worlds[world.toLowerCase()] = worldsSec.getString(world).toLowerCase()
            }
        }
    }
}