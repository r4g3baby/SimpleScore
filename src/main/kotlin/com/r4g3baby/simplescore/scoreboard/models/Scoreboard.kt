package com.r4g3baby.simplescore.scoreboard.models

data class Scoreboard(
    val name: String,
    val titles: List<Line> = emptyList(),
    val scores: Map<Int, List<Line>> = emptyMap(),
    val conditions: List<Condition> = emptyList()
) : Conditional(conditions) {
    internal fun asPlayerScoreboard(): PlayerScoreboard {
        return PlayerScoreboard(
            name = name,
            titles = titles.map { line -> PlayerLine(line.frames, line.conditions) },
            scores = scores.mapValues { it.value.map { line -> PlayerLine(line.frames, line.conditions) } },
            conditions = conditions
        )
    }
}