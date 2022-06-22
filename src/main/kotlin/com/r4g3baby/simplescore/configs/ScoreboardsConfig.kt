package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.scoreboard.models.Condition
import com.r4g3baby.simplescore.scoreboard.models.Frame
import com.r4g3baby.simplescore.scoreboard.models.Line
import com.r4g3baby.simplescore.scoreboard.models.Scoreboard
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
            val renderTime = scoreboardSec.getInt("renderTime", 5)
            val conditions = scoreboardSec.getConditions()

            val titles = scoreboardSec.getLineList("titles", updateTime, renderTime).let { titles ->
                if (titles == null) {
                    val titlesValue = scoreboardSec.get("titles")
                    plugin.logger.warning(
                        "Invalid titles value for scoreboard: $scoreboard, value: $titlesValue."
                    )
                }
                return@let titles ?: emptyList()
            }

            val scores = HashMap<Int, List<Line>>()
            when {
                scoreboardSec.isConfigurationSection("scores") -> {
                    val scoresSec = scoreboardSec.getConfigurationSection("scores")
                    scoresSec.getKeys(false).mapNotNull { it.toIntOrNull() }.forEach { score ->
                        scores[score] = scoresSec.getLineList(score.toString(), updateTime, renderTime).let { lines ->
                            if (lines == null) {
                                val scoreValue = scoreboardSec.get(score.toString())
                                plugin.logger.warning(
                                    "Invalid score value for scoreboard: $scoreboard, score: $score, value: $scoreValue."
                                )
                            }
                            return@let lines ?: emptyList()
                        }
                    }
                }
                scoreboardSec.isList("scores") -> scoreboardSec.getMapList("scores").forEach { scoreMap ->
                    val scoreNumber = scoreMap["score"] as Int
                    val scoreUpdateTime = scoreMap.getOrDefault("updateTime", updateTime) as Int
                    val scoreRenderTime = scoreMap.getOrDefault("renderTime", renderTime) as Int

                    scores[scoreNumber] = scoreMap.getLineList(scoreUpdateTime, scoreRenderTime) ?: emptyList()
                }
                else -> {
                    val scoresValue = scoreboardSec.get("scores")
                    plugin.logger.warning(
                        "Invalid scores value for scoreboard: $scoreboard, value: $scoresValue."
                    )
                }
            }

            scoreboards[scoreboard.lowercase()] = Scoreboard(scoreboard, titles, scores, conditions)
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
        return when {
            isList("conditions") -> getStringList("conditions").mapNotNull { name ->
                if (name.startsWith("!")) {
                    conditions[name.substring(1)]?.negate(true)
                } else conditions[name]
            }
            isString("conditions") -> getString("conditions").let { name ->
                if (name.startsWith("!")) {
                    conditions[name.substring(1)]?.negate(true)
                } else conditions[name]
            }?.let { listOf(it) } ?: emptyList()
            else -> emptyList()
        }
    }

    private fun ConfigurationSection.getLineList(path: String, updateTime: Int, renderTime: Int): List<Line>? {
        if (!contains(path)) return null

        when {
            isConfigurationSection(path) -> getConfigurationSection(path).let { section ->
                val frames = ArrayList<Frame>()
                when {
                    section.isList("frames") -> section.getList("frames").forEach { frame ->
                        parseFrame(frame, updateTime, renderTime)?.also { frames.add(it) }
                    }
                    section.isString("frames") -> frames.add(Frame(section.getString("frames"), updateTime, renderTime))
                }

                val conditions = getConditions()
                val elseLines = section.getLineList("else", updateTime, renderTime)

                return if (conditions.isNotEmpty() && elseLines != null) {
                    mutableListOf(Line(frames, conditions)).also { it.addAll(elseLines) }
                } else listOf(Line(frames, conditions))
            }
            isList(path) -> {
                val frames = ArrayList<Frame>()
                getList(path).forEach { frame ->
                    parseFrame(frame, updateTime, renderTime)?.also { frames.add(it) }
                }
                return listOf(Line(frames))
            }
            isString(path) -> return listOf(Line(listOf(Frame(getString(path), updateTime, renderTime))))
            else -> return null
        }
    }

    private fun Map<*, *>.getLineList(updateTime: Int, renderTime: Int): List<Line>? {
        if (!containsKey("frames")) return null

        val frames = when (val frames = get("frames")) {
            is List<*> -> {
                val frameList = ArrayList<Frame>()
                frames.forEach { frame ->
                    parseFrame(frame, updateTime, renderTime)?.also { frameList.add(it) }
                }
                frameList
            }
            is String -> {
                listOf(Frame(frames, updateTime, renderTime))
            }
            else -> emptyList()
        }

        val conditions = if (containsKey("conditions")) {
            (get("conditions") as List<*>).filterIsInstance<String>().mapNotNull { conditions[it] }
        } else emptyList()

        return if (conditions.isNotEmpty() && containsKey("else")) {
            val elseLines = (get("else") as Map<*, *>).getLineList(updateTime, renderTime)
            mutableListOf(Line(frames, conditions)).also {
                if (elseLines != null) it.addAll(elseLines)
            }
        } else listOf(Line(frames, conditions))
    }

    private fun parseFrame(frame: Any?, updateTime: Int, renderTime: Int): Frame? {
        return when (frame) {
            is String -> Frame(frame, updateTime, renderTime)
            is Map<*, *> -> Frame(
                frame["text"] as String,
                frame.getOrDefault("update", updateTime) as Int,
                frame.getOrDefault("render", renderTime) as Int
            )
            else -> null
        }
    }
}