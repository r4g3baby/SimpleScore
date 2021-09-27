package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.scoreboard.models.Condition
import com.r4g3baby.simplescore.scoreboard.models.ScoreLines
import com.r4g3baby.simplescore.scoreboard.models.BoardScore
import com.r4g3baby.simplescore.scoreboard.models.Scoreboard
import com.r4g3baby.simplescore.utils.configs.ConfigFile
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.Plugin

class ScoreboardsConfig(plugin: Plugin) : ConfigFile(plugin, "scoreboards") {
    private val conditionsConfig = ConditionsConfig(plugin)
    val conditions get() = conditionsConfig.conditions
    val scoreboards = HashMap<String, Scoreboard>()

    init {
        for (scoreboard in config.getKeys(false).filter { !scoreboards.containsKey(it.lowercase()) }) {
            if (config.isConfigurationSection(scoreboard)) {
                val scoreboardSec = config.getConfigurationSection(scoreboard)
                val updateTime = scoreboardSec.getInt("updateTime", 20)

                val titles = ScoreLines()
                scoreboardSec.getList("titles").forEach {
                    when (it) {
                        is String -> titles.add(it, updateTime)
                        is Map<*, *> -> titles.add(
                            it["text"] as String,
                            it.getOrDefault("time", updateTime) as Int
                        )
                        else -> {
                            plugin.logger.warning("Failed to parse titles expected String or Map but got ${it!!::class.java} instead.")
                        }
                    }
                }

                val scores = ArrayList<BoardScore>()
                if (scoreboardSec.isConfigurationSection("scores")) {
                    val scoresSec = scoreboardSec.getConfigurationSection("scores")
                    scoresSec.getKeys(false).mapNotNull { it.toIntOrNull() }.forEach { score ->
                        when {
                            scoresSec.isConfigurationSection(score.toString()) -> {
                                val scoreSec = scoresSec.getConfigurationSection(score.toString())
                                val scoreLines = ScoreLines()
                                scoreSec.getList("lines").forEach { line ->
                                    when (line) {
                                        is String -> scoreLines.add(line, updateTime)
                                        is Map<*, *> -> scoreLines.add(
                                            line["text"] as String,
                                            line.getOrDefault("time", updateTime) as Int
                                        )
                                        else -> {
                                            plugin.logger.warning(
                                                "Failed to parse lines for score \"$score\" expected String or Map but got ${line!!::class.java} instead."
                                            )
                                        }
                                    }
                                }
                                BoardScore(score, scoreLines, getConditions(scoreSec))
                            }
                            scoresSec.isList(score.toString()) -> {
                                val scoreLines = ScoreLines()
                                scoresSec.getList(score.toString()).forEach { line ->
                                    when (line) {
                                        is String -> scoreLines.add(line, updateTime)
                                        is Map<*, *> -> scoreLines.add(
                                            line["text"] as String,
                                            line.getOrDefault("time", updateTime) as Int
                                        )
                                        else -> {
                                            plugin.logger.warning(
                                                "Failed to parse lines for score \"$score\" expected String or Map but got ${line!!::class.java} instead."
                                            )
                                        }
                                    }
                                }
                                BoardScore(score, scoreLines)
                            }
                            else -> {
                                val scoreValue = scoresSec.get(score.toString())
                                plugin.logger.warning(
                                    "Failed to parse score \"$score\", expected a Map or List but got ${scoreValue::class.java} instead."
                                )
                                null
                            }
                        }?.also { scores.add(it) }
                    }
                }

                val name = scoreboard.lowercase()
                scoreboards[name] = Scoreboard(name, titles, scores, getConditions(scoreboardSec))
            }
        }
    }

    private fun getConditions(section: ConfigurationSection): List<Condition> {
        return section.getStringList("conditions").mapNotNull { conditions[it.lowercase()] }
    }
}