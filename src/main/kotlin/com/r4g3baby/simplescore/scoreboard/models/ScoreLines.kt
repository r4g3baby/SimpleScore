package com.r4g3baby.simplescore.scoreboard.models

data class ScoreLines(
    private val frames: MutableList<ScoreFrame> = ArrayList()
) : Iterable<ScoreFrame> {
    private var currentIndex = 0
    private var currentTick = 0

    val currentText: String?
        get() {
            if (frames.isEmpty()) return null
            return frames[currentIndex].text
        }

    val currentTime: Int?
        get() {
            if (frames.isEmpty()) return null
            return frames[currentIndex].time
        }

    fun addFrame(text: String, time: Int) = addFrame(ScoreFrame(text, time))
    fun addFrame(line: ScoreFrame) {
        frames.add(line)
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

    override fun iterator(): Iterator<ScoreFrame> {
        return frames.iterator()
    }
}