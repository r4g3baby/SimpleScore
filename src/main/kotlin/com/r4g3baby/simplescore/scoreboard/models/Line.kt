package com.r4g3baby.simplescore.scoreboard.models

data class Line(
    val frames: List<Frame> = emptyList(),
    val conditions: List<Condition> = emptyList()
) : Conditional(conditions), Iterable<Frame> {
    override fun iterator(): Iterator<Frame> {
        return frames.iterator()
    }
}