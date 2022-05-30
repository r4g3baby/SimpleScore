package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.scoreboard.models.*
import com.r4g3baby.simplescore.utils.configs.ConfigFile
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.Plugin

class ScoreboardsConfig(
    plugin: Plugin, private val conditions: ConditionsConfig
) : ConfigFile(plugin, "scoreboards"), Iterable<Map.Entry<String, Scoreboard>> {
    private val scoreboards = HashMap<String, Scoreboard>()

    init {
        config.getKeys(false).filter { !scoreboards.containsKey(it.lowercase()) }.forEach { scoreboard ->
            if (!config.isConfigurationSection(scoreboard)) return@forEach

            val scoreboardSec = config.getConfigurationSection(scoreboard)
            val updateTime = scoreboardSec.getInt("updateTime", 20)
            val conditions = scoreboardSec.getConditions()

            val titles = scoreboardSec.getScoreFrames("titles", updateTime)
            if (titles == null) {
                val titlesValue = scoreboardSec.get("titles")
                plugin.logger.warning(
                    "Invalid titles value for scoreboard: $scoreboard, value: $titlesValue."
                )
            }

            val scores = ArrayList<BoardScore>()
            when {
                scoreboardSec.isConfigurationSection("scores") -> {
                    val scoresSec = scoreboardSec.getConfigurationSection("scores")
                    scoresSec.getKeys(false).mapNotNull { it.toIntOrNull() }.forEach { score ->
                        val scoreFrames = scoresSec.getScoreFrames(score.toString(), updateTime)
                        if (scoreFrames == null) {
                            val scoreValue = scoreboardSec.get(score.toString())
                            plugin.logger.warning(
                                "Invalid score value for scoreboard: $scoreboard, score: $score, value: $scoreValue."
                            )
                        }

                        scores.add(BoardScore(score, scoreFrames ?: ScoreFrames()))
                    }
                }
                scoreboardSec.isList("scores") -> scoreboardSec.getMapList("scores").forEach { scoreMap ->
                    val scoreNumber = scoreMap["score"] as Int
                    val scoreUpdateTime = scoreMap.getOrDefault("updateTime", updateTime) as Int
                    val scoreFrames = scoreMap.getScoreFrames(scoreUpdateTime)
                    scores.add(BoardScore(scoreNumber, scoreFrames ?: ScoreFrames()))
                }
                else -> {
                    val scoresValue = scoreboardSec.get("scores")
                    plugin.logger.warning(
                        "Invalid scores value for scoreboard: $scoreboard, value: $scoresValue."
                    )
                }
            }

            scoreboards[scoreboard.lowercase()] = Scoreboard(
                scoreboard, titles ?: ScoreFrames(), scores, conditions
            )
        }
    }

    val keys: Set<String> get() = scoreboards.keys
    val values: Collection<Scoreboard> get() = scoreboards.values

    operator fun get(scoreboard: String): Scoreboard? {
        return scoreboards[scoreboard.lowercase()]
    }

    override fun iterator(): Iterator<Map.Entry<String, Scoreboard>> {
        return scoreboards.iterator()
    }

    private fun ConfigurationSection.getConditions(): List<Condition> {
        if (!isList("conditions")) return emptyList()
        return getStringList("conditions").mapNotNull { conditions[it] }
    }

    private fun ConfigurationSection.getScoreFrames(path: String, updateTime: Int): ScoreFrames? {
        if (!contains(path)) return null

        when {
            isConfigurationSection(path) -> getConfigurationSection(path).let { section ->
                val frames = ArrayList<ScoreFrame>()
                when {
                    section.isList("frames") -> section.getList("frames").forEach { frame ->
                        parseFrame(frame, updateTime)?.also { frames.add(it) }
                    }
                    section.isString("frames") -> frames.add(ScoreFrame(section.getString("frames"), updateTime))
                }

                return ScoreFrames(frames, section.getScoreFrames("else", updateTime), getConditions())
            }
            isList(path) -> {
                val frames = ArrayList<ScoreFrame>()
                getList(path).forEach { frame ->
                    parseFrame(frame, updateTime)?.also { frames.add(it) }
                }
                return ScoreFrames(frames)
            }
            isString(path) -> return ScoreFrames(listOf(ScoreFrame(getString(path), updateTime)))
            else -> return null
        }
    }

    private fun Map<*, *>.getScoreFrames(updateTime: Int): ScoreFrames? {
        if (!containsKey("frames")) return null

        val frames = when (val frames = get("frames")) {
            is List<*> -> {
                val frameList = ArrayList<ScoreFrame>()
                frames.forEach { frame ->
                    parseFrame(frame, updateTime)?.also { frameList.add(it) }
                }
                frameList
            }
            is String -> {
                listOf(ScoreFrame(frames, updateTime))
            }
            else -> emptyList()
        }

        val elseFrames = if (containsKey("else")) {
            (get("else") as Map<*, *>).getScoreFrames(updateTime)
        } else null

        val conditions = if (containsKey("conditions")) {
            (get("conditions") as List<*>).filterIsInstance<String>().mapNotNull {
                conditions[it]
            }
        } else emptyList()

        return ScoreFrames(frames, elseFrames, conditions)
    }

    private fun parseFrame(frame: Any?, updateTime: Int): ScoreFrame? {
        return when (frame) {
            is String -> ScoreFrame(frame, updateTime)
            is Map<*, *> -> ScoreFrame(frame["text"] as String, frame.getOrDefault("time", updateTime) as Int)
            else -> null
        }
    }
}