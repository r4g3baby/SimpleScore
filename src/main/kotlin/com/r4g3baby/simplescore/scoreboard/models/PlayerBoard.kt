package com.r4g3baby.simplescore.scoreboard.models

data class PlayerBoard(
    var title: String,
    var scores: Map<Int, String>
) {
    fun getScore(entry: String): Int? {
        return scores.entries.find { it.value == entry }?.key
    }
}
