package com.zephbyte.scaleddragonfight

import com.zephbyte.scaleddragonfight.ConfigManager.enableMod // Direct access to config
import com.zephbyte.scaleddragonfight.mixin.EnderDragonFightAccessor
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.level.dimension.end.EndDragonFight
import net.minecraft.world.entity.boss.enderdragon.EndCrystal
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.chat.Component
import net.minecraft.world.level.Level
import kotlin.math.ceil

object DragonEventHandler {

    private data class DelayState(
        var delayActive: Boolean = false,
        var ticksRemaining: Int = 0,
        var initialSpawnAttemptProcessed: Boolean = false, // To ensure we only delay the *very first* attempt
        var fightInstance: EndDragonFight? = null // To store the fight instance for later spawning
    )

    private val levelDelayStates = mutableMapOf<ServerLevel, DelayState>()

    fun register() {
        ServerEntityEvents.ENTITY_LOAD.register { entity, level ->
            // Check enableMod from ConfigManager
            if (!enableMod) {
                return@register // If mod is disabled, do nothing
            }

            if (level is ServerLevel && entity is EnderDragon && level.dimension() == Level.END) {
                LOGGER.info("Ender Dragon detected loading in The End! Checking scaling conditions...")
                // Delegate the actual scaling logic to DragonScaler
                DragonScaler.scaleDragon(entity, level)
            }
        }

        // Register for world ticks in The End to manage the countdown
        ServerTickEvents.END_WORLD_TICK.register(::onWorldTick)

        LOGGER.info("DragonEventHandler registered for spawn delay and scaling.")
    }

    /**
     * Called from EnderDragonFightMixin to intercept the initial dragon spawn.
     * Returns true if the original spawn should be cancelled (i.e., we are delaying it).
     */
    fun onInitialDragonPreSpawn(fight: EndDragonFight, level: ServerLevel): Boolean {
        if (level.dimension() != Level.END) return false // Should always be The End from the mixin context

        val state = levelDelayStates.computeIfAbsent(level) { DelayState() }

        // Case 1: Feature is disabled in config
        if (!ConfigManager.enableInitialSpawnDelay) {
            state.initialSpawnAttemptProcessed = true // Mark as processed, feature disabled
            if (state.delayActive) { // If a delay was somehow active and then config got disabled
                state.delayActive = false
                state.ticksRemaining = 0 // This will ensure any active countdown stops and tries to spawn
                LOGGER.info("Initial spawn delay was active but config is now disabled. Dragon will attempt to spawn.")
            }
            return false // Do not cancel, let vanilla spawn proceed
        }

        // Case 2: Our timer has finished, and we are now programmatically triggering the spawn.
        // `initialSpawnAttemptProcessed` will be true, and `delayActive` will be false.
        if (state.initialSpawnAttemptProcessed && !state.delayActive) {
            LOGGER.debug("Permitting dragon spawn triggered by delay completion.")
            return false // Do not cancel, let our spawn proceed
        }

        // Case 3: Delay is currently active (timer is ticking down).
        // This handles if vanilla code tries to spawn again while our timer is running.
        if (state.delayActive) {
            LOGGER.debug("Dragon spawn attempt while delay is_active. Cancelling vanilla spawn.")
            return true // Cancel subsequent vanilla spawn attempts
        }

        // Case 4: This is the first time for this world, feature enabled, not yet processed, not active.
        // This is where we initiate the delay.
        // (state.initialSpawnAttemptProcessed is false here, or it would have hit Case 2 or 3)
        LOGGER.info("Initial Ender Dragon spawn in ${level.dimension()} will be delayed by ${ConfigManager.initialSpawnDelaySeconds} seconds.")
        state.delayActive = true
        state.ticksRemaining = ConfigManager.initialSpawnDelaySeconds * 20 // 20 ticks per second
        state.initialSpawnAttemptProcessed = true // Mark that we've handled the first attempt
        state.fightInstance = fight // Store the fight instance to trigger spawn later

        return true // Cancel the immediate vanilla spawn
    }

    private fun onWorldTick(level: ServerLevel) {
        // This method is registered for END_WORLD_TICK, so world..dimension() should be World.END
        // but a check doesn't hurt if you ever change registration.
        if (level.dimension() != Level.END) return

        val state = levelDelayStates[level] ?: return // No state for this world, or delay not initiated

        // Handle if feature gets disabled mid-countdown (e.g., via /reload and config change)
        if (!ConfigManager.enableInitialSpawnDelay && state.delayActive) {
            LOGGER.info("Initial spawn delay feature disabled mid-countdown for ${level.dimension()}. Triggering dragon spawn now.")
            state.delayActive = false
            state.ticksRemaining = 0 // This will trigger the spawn logic below immediately
        }

        if (state.delayActive && state.ticksRemaining > 0) {
            state.ticksRemaining--

            if (ConfigManager.showSpawnDelayCountdown) {
                val remainingSeconds = ceil(state.ticksRemaining / 20.0).toInt()
                if (remainingSeconds > 0) { // Only show if time is actually remaining
                    val message = Component.literal("Dragon spawning in: $remainingSeconds...")
                    level.players().forEach { player ->
                        player.displayClientMessage(message, true) // 'true' sends to action bar
                    }
                }
            }

            if (state.ticksRemaining <= 0) {
                LOGGER.info("Initial spawn delay finished for ${level.dimension()}. Triggering Ender Dragon spawn.")
                state.delayActive = false // Stop the delay state

                state.fightInstance?.let { fight ->
                    // This call will go through the EnderDragonFightMixin again.
                    // onInitialDragonPreSpawn will see:
                    // initialSpawnAttemptProcessed = true, delayActive = false.
                    // So it returns false (don't cancel), allowing respawnDragon to proceed.
                    // Cast to the accessor and call the invoker method
                    if (fight is EnderDragonFightAccessor) {
                        fight.callRespawnDragon(emptyList<EndCrystal>()) // Use the accessor
                        LOGGER.info("Ender Dragon respawn initiated by the mod after delay via accessor.")
                    } else {
                        LOGGER.error("Could not cast EnderDragonFight to EnderDragonFightAccessor. Dragon not spawned post-delay.")
                    }
                } ?: LOGGER.error("EnderDragonFight instance was null when trying to spawn dragon post-delay for ${level.dimension()}")
            }
        }
    }
}