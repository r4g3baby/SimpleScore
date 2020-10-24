package com.r4g3baby.simplescore.scoreboard.models

data class Scoreboard(
    val titles: ScoreLine,
    val scores: Map<Int, ScoreLine>
)