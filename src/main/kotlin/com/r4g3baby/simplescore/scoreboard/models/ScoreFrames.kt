package com.r4g3baby.simplescore.scoreboard.models

import org.bukkit.entity.Player

data class ScoreFrames(
    private val frames: List<ScoreFrame> = emptyList(),
    val elseFrames: ScoreFrames? = null,
    val conditions: List<Condition> = emptyList()
) : Iterable<ScoreFrame> {
    private var currentIndex = 0
    private var currentTick = 0

    val current: ScoreFrame?
        get() {
            if (frames.isEmpty()) return null
            return frames[currentIndex]
        }

    fun tick() {
        if (frames.isEmpty()) return
        if (currentTick++ >= frames[currentIndex].time) {
            if (currentIndex++ >= (frames.size - 1)) {
                currentIndex = 0
            }
            currentTick = 1
        }
    }

    fun canSee(player: Player): Boolean {
        return conditions.isEmpty() || !conditions.any { !it.check(player) }
    }

    override fun iterator(): Iterator<ScoreFrame> {
        return frames.iterator()
    }
}