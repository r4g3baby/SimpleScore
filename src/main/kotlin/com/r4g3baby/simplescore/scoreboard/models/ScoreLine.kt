package com.r4g3baby.simplescore.scoreboard.models

class ScoreLine : Cloneable {
    private var lines = ArrayList<String>()
    private var timings = ArrayList<Int>()

    fun add(text: String, time: Int) {
        lines.add(text)
        timings.add(time)
    }

    private var current: Int = 0
    private var frames: Int = 0
    fun nextFrame(): String {
        if (frames >= timings[current]) {
            current++
            if (current >= lines.size) {
                current = 0
            }
            frames = 0
        }
        frames++
        return lines[current]
    }

    public override fun clone(): ScoreLine {
        val clone = super.clone() as ScoreLine
        clone.lines = ArrayList(lines)
        clone.timings = ArrayList(timings)
        clone.current = current
        clone.frames = frames
        return clone
    }
}