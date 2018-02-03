package com.r4g3baby.simplescore.scoreboard.models

import java.util.*

data class ScoreboardWorld(val titles: Queue<String>, val scores: Map<Int, Queue<String>>)