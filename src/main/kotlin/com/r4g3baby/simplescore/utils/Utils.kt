package com.r4g3baby.simplescore.utils

fun List<String>.isEqual(other: List<String>): Boolean {
    if (this.size != other.size) {
        return false
    }

    return this.toTypedArray() contentEquals other.toTypedArray()
}