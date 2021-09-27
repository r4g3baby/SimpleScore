package com.r4g3baby.simplescore.scoreboard.models

data class ScoreLines(
    private val lines: ArrayList<String> = ArrayList(),
    private val timings: ArrayList<Int> = ArrayList()
) {
    private var current = 0
    private var ticks = 0

    fun add(text: String, time: Int) {
        lines.add(text)
        timings.add(time)
    }

    fun current(): String {
        return lines[current]
    }

    fun next(): String {
        if (ticks >= timings[current]) {
            current++
            if (current >= lines.size) {
                current = 0
            }
            ticks = 0
        }
        ticks++
        return lines[current]
    }
}