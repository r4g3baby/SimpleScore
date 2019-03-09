package com.r4g3baby.simplescore.scoreboard.models

import java.util.*

class ScoreLine {
    private var lines: Queue<String> = LinkedList()
    private var timings: Queue<Int> = LinkedList()

    fun add(text: String, time: Int) {
        lines.add(text)
        timings.add(time)
    }

    private var frames: Int = 0
    fun nextFrame(): String {
        if (frames >= timings.peek()) {
            lines.add(lines.poll())
            timings.add(timings.poll())
            frames = 0
        }
        frames++
        return lines.peek()
    }

    fun clone(): ScoreLine {
        val clone = ScoreLine()
        clone.lines = LinkedList(lines)
        clone.timings = LinkedList(timings)
        clone.frames = frames
        return clone
    }
}