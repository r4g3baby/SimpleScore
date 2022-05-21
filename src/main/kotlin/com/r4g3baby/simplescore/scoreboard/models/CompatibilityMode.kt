package com.r4g3baby.simplescore.scoreboard.models

enum class CompatibilityMode {
    DISABLE,
    BLOCK,
    NONE;

    companion object {
        @JvmStatic
        fun fromValue(value: String): CompatibilityMode {
            for (mode in values()) {
                if (mode.name.equals(value, ignoreCase = true)) {
                    return mode
                }
            }
            return NONE
        }
    }
}