package com.r4g3baby.simplescore.scoreboard.models

data class PlayerLine(
    private val frames: List<Frame>,
    private val conditions: List<Condition>
) : Conditional(conditions) {
    private var currentIndex = 0
    private var currentTick = 1

    val currentText: String?
        get() {
            if (frames.isEmpty()) return null
            return frames[currentIndex].text
        }

    val shouldRender: Boolean
        get() {
            if (frames.isEmpty()) return true

            // If the current tick is 1 we know the frame just changed
            if (currentTick == 1) return true
            val frame = frames[currentIndex]

            // Will render at the start of the mext frame instead
            if (frame.updateTime == currentTick && frame.renderTime == currentTick) return false
            return (currentTick % frame.renderTime) == 0
        }

    fun tick() {
        if (frames.isEmpty()) return
        if (currentTick++ >= frames[currentIndex].updateTime) {
            if (currentIndex++ >= (frames.size - 1)) {
                currentIndex = 0
            }
            currentTick = 1
        }
    }
}
