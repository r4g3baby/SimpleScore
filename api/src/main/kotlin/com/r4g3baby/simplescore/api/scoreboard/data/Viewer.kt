package com.r4g3baby.simplescore.api.scoreboard.data

import com.r4g3baby.simplescore.api.scoreboard.Scoreboard
import java.lang.ref.WeakReference

/**
 * Represents a player that is capable of viewing a scoreboard and managing its visibility.
 * This interface serves as the core component for managing individual player's scoreboard state,
 * handling multiple scoreboard providers, and managing display priorities.
 *
 * Key features:
 * - Manages multiple scoreboards from different sources (plugins/services)
 * - Implements a priority system for scoreboard display
 * - Handles scoreboard visibility states
 * - Provides memory-safe player references
 *
 * Example usage:
 * ```kotlin
 * val viewer = Viewer(player)
 * val provider = Provider("MyPlugin", "game")
 *
 * // Set up a scoreboard with high priority
 * viewer.setScoreboard(gameScoreboard, provider, Priority.High)
 *
 * // Temporarily hide the scoreboard
 * viewer.hideScoreboard(provider)
 * ```
 *
 * @param V The type of the platform-specific player object.
 */

public interface Viewer<V : Any> {
    /**
     * Holds a weak reference to a viewer object of type [V]. This reference allows
     * the viewer to be accessed while still allowing it to be garbage collected if
     * no strong references exist.
     */
    public val reference: WeakReference<V>

    /**
     * Represents the current scoreboard associated with a viewer within the system.
     * This property reflects the highest-priority visible scoreboard among all registered
     * scoreboards for this viewer.
     */
    public val scoreboard: Scoreboard<V>?

    /**
     * Indicates whether the scoreboard is currently hidden for the viewer.
     * The scoreboard is considered hidden if any plugin/service has requested
     * it to be hidden through [hideScoreboard].
     *
     * Note: Individual hide requests are stacked - the scoreboard will remain hidden
     * until all plugins that requested to hide it call [showScoreboard].
     */
    public val isScoreboardHidden: Boolean

    /**
     * Sets a scoreboard for a specific viewer with a default priority of [Priority.Normal].
     *
     * The provider system allows multiple plugins to manage scoreboards independently
     * without interfering with each other. Each plugin should use a unique provider
     * to prevent conflicts with other plugins.
     *
     * Example usage:
     * ```kotlin
     * val provider = Provider("MyPlugin", "lobby")
     * viewer.setScoreboard(myScoreboard, provider)
     * ```
     *
     * @param scoreboard The scoreboard to be associated with the viewer. If null, removes the existing scoreboard
     * @param provider The provider that is setting the scoreboard
     */
    public fun setScoreboard(scoreboard: Scoreboard<V>?, provider: Provider): Unit = setScoreboard(
        scoreboard, provider, Priority.Normal
    )

    /**
     * Sets a scoreboard for a specific viewer with the specified priority.
     *
     * The priority system determines which scoreboard is displayed when multiple plugins
     * set different scoreboards for the same viewer. Higher priority scoreboards take
     * precedence over lower priority ones. When scoreboards have the same priority,
     * the most recently set scoreboard will be displayed.
     *
     * Example usage:
     * ```kotlin
     * val gameProvider = Provider("MyPlugin", "game")
     * viewer.setScoreboard(gameScoreboard, gameProvider, Priority.High)
     * ```
     *
     * @param scoreboard The scoreboard to be associated with the viewer. If null, removes the existing scoreboard
     * @param provider The provider that is setting the scoreboard
     * @param priority The priority level for this scoreboard
     * @see Priority
     */
    public fun setScoreboard(scoreboard: Scoreboard<V>?, provider: Provider, priority: Priority)

    /**
     * Retrieves the scoreboard currently associated with a specific provider.
     * This method allows plugins to check their current scoreboard without affecting
     * the display state.
     *
     * @param provider The provider whose scoreboard should be retrieved
     * @return The Scoreboard currently set by the specified provider, or null if none exists
     */
    public fun getScoreboard(provider: Provider): Scoreboard<V>?

    /**
     * Removes the scoreboard associated with a specific provider.
     *
     * @param provider The provider whose scoreboard should be removed
     * @return The removed scoreboard, or null if none existed
     */
    public fun removeScoreboard(provider: Provider): Scoreboard<V>?

    /**
     * Sets a priority level for operations associated with a specific provider.
     * This priority affects how this plugin's scoreboard is displayed relative to other
     * plugins' scoreboards.
     *
     * The priority setting persists even if the scoreboard is temporarily removed,
     * allowing consistent priority management across multiple operations.
     *
     * Example usage:
     * ```kotlin
     * // Set high priority for important game states
     * viewer.setPriority(Priority.High, gameProvider)
     *
     * // Remove custom priority, reverting to default
     * viewer.setPriority(null, provider)
     * ```
     *
     * @param priority The priority level to be assigned, or null to reset to default
     * @param provider The provider whose priority should be changed
     */
    public fun setPriority(priority: Priority?, provider: Provider)

    /**
     * Retrieves the current priority level set for a specific provider.
     * This allows plugins to check their current priority status and make
     * decisions based on it.
     *
     * Priority levels affect the visibility of scoreboards when multiple providers
     * are attempting to display scoreboards simultaneously. Higher priority scoreboards
     * take precedence over lower priority ones.
     *
     * Example usage:
     * ```kotlin
     * val currentPriority = viewer.getPriority(provider)
     * if (currentPriority == null || currentPriority < Priority.High) {
     *     viewer.setPriority(Priority.High, provider)
     * }
     * ```
     *
     * @param provider The provider whose priority level should be retrieved
     * @return The current Priority level, or null if using default priority
     */
    public fun getPriority(provider: Provider): Priority?

    /**
     * Temporarily hides the scoreboard for this viewer, as requested by a specific
     * provider. Multiple providers can request to hide the scoreboard, and it
     * will remain hidden until all hiding requests are cleared.
     *
     * This operation doesn't remove the scoreboard assignment, it only affects
     * the display state.
     *
     * @param provider The provider requesting to hide the scoreboard
     * @return true if the provider had no pending hide requests, false otherwise
     */
    public fun hideScoreboard(provider: Provider): Boolean

    /**
     * Removes a hide request for the scoreboard from a specific provider.
     * The scoreboard will become visible again only when all providers that requested
     * to hide it have called this method.
     *
     * @param provider The provider that previously requested to hide the scoreboard
     * @return true if the provider had a pending hide request, false otherwise
     */
    public fun showScoreboard(provider: Provider): Boolean

    /**
     * Checks if a specific provider has requested to hide the scoreboard.
     * This can be used to verify the current hiding state for a particular identifier.
     *
     * @param provider The provider to check
     * @return true if the specified provider has requested to hide the scoreboard, false otherwise
     */
    public fun isHidingScoreboard(provider: Provider): Boolean
}
