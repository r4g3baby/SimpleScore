package com.r4g3baby.simplescore.scoreboard.models

data class ScoreLines(
    private val lines: MutableList<ScoreLine> = ArrayList()
) : Iterable<ScoreLine> {
    private var currentIndex = 0
    private var currentTick = 0

    val currentText: String?
        get() {
            if (lines.isEmpty()) return null
            return lines[currentIndex].text
        }

    val currentTime: Int?
        get() {
            if (lines.isEmpty()) return null
            return lines[currentIndex].time
        }

    fun add(text: String, time: Int) = add(ScoreLine(text, time))
    fun add(line: ScoreLine) {
        lines.add(line)
    }

    fun tick() {
        if (lines.isEmpty()) return
        if (currentTick++ >= lines[currentIndex].time) {
            if (currentIndex++ >= (lines.size - 1)) {
                currentIndex = 0
            }
            currentTick = 1
        }
    }

    override fun iterator(): Iterator<ScoreLine> {
        return lines.iterator()
    }
}