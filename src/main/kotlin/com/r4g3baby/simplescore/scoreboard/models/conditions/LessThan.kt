package com.r4g3baby.simplescore.scoreboard.models.conditions

import com.r4g3baby.simplescore.scoreboard.models.Condition
import com.r4g3baby.simplescore.scoreboard.placeholders.PlaceholderReplacer.replacePlaceholders
import com.r4g3baby.simplescore.scoreboard.placeholders.VariablesReplacer.replaceVariables
import org.bukkit.entity.Player

data class LessThan(
    val input: String,
    val value: String,
    val orEqual: Boolean,
    override val negate: Boolean = false
) : Condition() {
    override fun can(player: Player): Boolean {
        val parsedInput = input.replacePlaceholders(player).replaceVariables(player)
        val compareResult = parsedInput.compareTo(value)
        return if (orEqual) compareResult <= 0 else compareResult < 0
    }

    override fun negate(negate: Boolean): Condition {
        return copy(negate = negate)
    }
}