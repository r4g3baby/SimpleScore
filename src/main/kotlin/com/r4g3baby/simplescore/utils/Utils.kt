package com.r4g3baby.simplescore.utils

fun List<Any>.isEqual(other: List<Any>): Boolean {
    if (this.size != other.size) {
        return false
    }

    return this.toTypedArray() contentEquals other.toTypedArray()
}
