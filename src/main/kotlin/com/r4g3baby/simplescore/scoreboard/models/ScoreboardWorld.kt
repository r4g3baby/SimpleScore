package com.r4g3baby.simplescore.scoreboard.models

data class ScoreboardWorld(val titles: ScoreLine, val scores: Map<Int, ScoreLine>)