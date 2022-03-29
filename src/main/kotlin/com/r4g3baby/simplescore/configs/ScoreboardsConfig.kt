package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.scoreboard.models.*
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

                val titleFrames = ArrayList<ScoreFrame>()
                when {
                    scoreboardSec.isList("titles") -> {
                        scoreboardSec.getList("titles").forEach { frame ->
                            val parsed = parseFrame(frame, updateTime)?.also { titleFrames.add(it) }
                            if (parsed == null) plugin.logger.warning(
                                "Invalid titles frame value for scoreboard: $scoreboard, value: $frame."
                            )
                        }
                    }
                    scoreboardSec.isString("titles") -> {
                        titleFrames.add(ScoreFrame(scoreboardSec.getString("titles"), updateTime))
                    }
                    else -> {
                        val titlesValue = scoreboardSec.get("titles")
                        plugin.logger.warning(
                            "Invalid titles value for scoreboard: $scoreboard, value: $titlesValue."
                        )
                    }
                }

                val scores = ArrayList<BoardScore>()
                when {
                    scoreboardSec.isConfigurationSection("scores") -> {
                        val scoresSec = scoreboardSec.getConfigurationSection("scores")
                        scoresSec.getKeys(false).mapNotNull { it.toIntOrNull() }.forEach { score ->
                            val scoreFrames = ArrayList<ScoreFrame>()
                            val elseFrames = ArrayList<ScoreFrame>()
                            var conditions = emptyList<Condition>()

                            when {
                                scoresSec.isConfigurationSection(score.toString()) -> {
                                    val scoreSec = scoresSec.getConfigurationSection(score.toString())
                                    when {
                                        scoreSec.isList("frames") -> {
                                            scoreSec.getList("frames").forEach { line ->
                                                val parsed = parseFrame(line, updateTime)?.also { scoreFrames.add(it) }
                                                if (parsed == null) plugin.logger.warning(
                                                    "Invalid frame value for scoreboard: $scoreboard, score: $score, value: $line."
                                                )
                                            }
                                        }
                                        scoreSec.isString("frames") -> {
                                            scoreFrames.add(ScoreFrame(scoreSec.getString("frames"), updateTime))
                                        }
                                        else -> {
                                            val framesValue = scoreSec.get("frames")
                                            plugin.logger.warning(
                                                "Invalid frames value for scoreboard: $scoreboard, score: $score, value: $framesValue."
                                            )
                                        }
                                    }

                                    when {
                                        scoreSec.isConfigurationSection("else") -> {
                                            val elseSec = scoreSec.getConfigurationSection("else")
                                            when {
                                                elseSec.isList("frames") -> {
                                                    elseSec.getList("frames").forEach { line ->
                                                        val parsed = parseFrame(line, updateTime)?.also { scoreFrames.add(it) }
                                                        if (parsed == null) plugin.logger.warning(
                                                            "Invalid else frame value for scoreboard: $scoreboard, score: $score, value: $line."
                                                        )
                                                    }
                                                }
                                                elseSec.isString("frames") -> {
                                                    scoreFrames.add(ScoreFrame(elseSec.getString("frames"), updateTime))
                                                }
                                                else -> {
                                                    val framesValue = elseSec.get("frames")
                                                    plugin.logger.warning(
                                                        "Invalid else frames value for scoreboard: $scoreboard, score: $score, value: $framesValue."
                                                    )
                                                }
                                            }
                                        }
                                        scoreSec.isList("else") -> {
                                            scoresSec.getList("else").forEach { frame ->
                                                val parsed = parseFrame(frame, updateTime)?.also { elseFrames.add(it) }
                                                if (parsed == null) plugin.logger.warning(
                                                    "Invalid else frame value for scoreboard: $scoreboard, score: $score, value: $frame."
                                                )
                                            }
                                        }
                                        scoreSec.isString("else") -> {
                                            elseFrames.add(ScoreFrame(scoreSec.getString("else"), updateTime))
                                        }
                                    }

                                    conditions = getConditions(scoreSec)
                                }
                                scoresSec.isList(score.toString()) -> {
                                    scoresSec.getList(score.toString()).forEach { frame ->
                                        val parsed = parseFrame(frame, updateTime)?.also { scoreFrames.add(it) }
                                        if (parsed == null) plugin.logger.warning(
                                            "Invalid frame value for scoreboard: $scoreboard, score: $score, value: $frame."
                                        )
                                    }
                                }
                                scoresSec.isString(score.toString()) -> {
                                    scoreFrames.add(ScoreFrame(scoresSec.getString(score.toString()), updateTime))
                                }
                                else -> {
                                    val scoreValue = scoresSec.get(score.toString())
                                    plugin.logger.warning(
                                        "Invalid score value for scoreboard: $scoreboard, score: $score, value: $scoreValue."
                                    )
                                }
                            }

                            scores.add(BoardScore(score, ScoreFrames(scoreFrames), ScoreFrames(elseFrames), conditions))
                        }
                    }
                    scoreboardSec.isList("scores") -> {
                        scoreboardSec.getMapList("scores").forEach { scoreSec ->
                            val score = scoreSec["score"] as Int
                            val scoreFrames = ArrayList<ScoreFrame>()
                            val elseFrames = ArrayList<ScoreFrame>()
                            val conditions = if ("conditions" in scoreSec) {
                                (scoreSec["conditions"] as List<*>).filterIsInstance<String>().mapNotNull {
                                    conditions[it.lowercase()]
                                }
                            } else emptyList()

                            when (val frames = scoreSec["frames"]) {
                                is List<*> -> for (frame in frames) {
                                    val parsed = parseFrame(frame, updateTime)?.also { scoreFrames.add(it) }
                                    if (parsed == null) plugin.logger.warning(
                                        "Invalid frame value for scoreboard: $scoreboard, score: $score, value: $frame."
                                    )
                                }
                                is String -> scoreFrames.add(ScoreFrame(frames, updateTime))
                                else -> {
                                    plugin.logger.warning(
                                        "Invalid frames value for scoreboard: $scoreboard, value: $frames."
                                    )
                                }
                            }

                            if ("else" in scoreSec) {
                                when (val frames = scoreSec["else"]) {
                                    is List<*> -> for (frame in frames) {
                                        val parsed = parseFrame(frame, updateTime)?.also { elseFrames.add(it) }
                                        if (parsed == null) plugin.logger.warning(
                                            "Invalid else frame value for scoreboard: $scoreboard, score: $score, value: $frame."
                                        )
                                    }
                                    is String -> elseFrames.add(ScoreFrame(frames, updateTime))
                                    else -> {
                                        plugin.logger.warning(
                                            "Invalid else frames value for scoreboard: $scoreboard, value: $frames."
                                        )
                                    }
                                }
                            }

                            scores.add(BoardScore(score, ScoreFrames(scoreFrames), ScoreFrames(elseFrames), conditions))
                        }
                    }
                    else -> {
                        val scoresValue = scoreboardSec.get("scores")
                        plugin.logger.warning(
                            "Invalid scores value for scoreboard: $scoreboard, value: $scoresValue."
                        )
                    }
                }

                scoreboards[scoreboard.lowercase()] = Scoreboard(
                    scoreboard, ScoreFrames(titleFrames), scores, getConditions(scoreboardSec)
                )
            }
        }
    }

    private fun parseFrame(frame: Any?, updateTime: Int): ScoreFrame? {
        return when (frame) {
            is String -> ScoreFrame(frame, updateTime)
            is Map<*, *> -> ScoreFrame(frame["text"] as String, frame.getOrDefault("time", updateTime) as Int)
            else -> null
        }
    }

    private fun getConditions(section: ConfigurationSection): List<Condition> {
        if (!section.isList("conditions")) return emptyList()
        return section.getStringList("conditions").mapNotNull { conditions[it.lowercase()] }
    }
}