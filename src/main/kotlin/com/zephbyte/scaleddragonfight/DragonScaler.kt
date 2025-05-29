package com.zephbyte.scaleddragonfight

// Import specific config values needed, or the whole ConfigManager
import com.zephbyte.scaleddragonfight.ConfigManager.additionalHealthPerPlayer
import com.zephbyte.scaleddragonfight.ConfigManager.baseDragonHealth
import com.zephbyte.scaleddragonfight.ConfigManager.countCreativeModePlayers
import com.zephbyte.scaleddragonfight.ConfigManager.enableBroadcast
import com.zephbyte.scaleddragonfight.ConfigManager.scaleWithOnePlayer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text

object DragonScaler {

    private const val VANILLA_DRAGON_HEALTH = 200.0f // Vanilla default for comparison

    fun scaleDragon(dragon: EnderDragonEntity, world: ServerWorld) {
        // Filter players in The End and consider creative mode setting
        val presentPlayers = world.players.filterIsInstance<ServerPlayerEntity>()
        val eligiblePlayers = presentPlayers.filter { player ->
            countCreativeModePlayers || !player.isCreative
        }
        val scaleEligiblePlayerCount = eligiblePlayers.size

        // Determine the number of players that contribute to *additional* health
        val playersContributingToAdditionalHealth = when {
            !scaleWithOnePlayer && scaleEligiblePlayerCount <= 1 -> 0
            scaleWithOnePlayer && scaleEligiblePlayerCount >= 1 -> scaleEligiblePlayerCount
            else -> scaleEligiblePlayerCount - 1
        }

        val newMaxHealth = baseDragonHealth + (additionalHealthPerPlayer * playersContributingToAdditionalHealth)

        // Ensure health doesn't go below the configured base
        val finalMaxHealth = newMaxHealth.coerceAtLeast(baseDragonHealth)

        dragon.getAttributeInstance(EntityAttributes.MAX_HEALTH)?.let { attributeInstance ->
            attributeInstance.baseValue = finalMaxHealth.toDouble()
            dragon.health = finalMaxHealth // Set current health to new max health
            LOGGER.info("Scaled Ender Dragon health. Eligible Players: $scaleEligiblePlayerCount, Players Adding Health: $playersContributingToAdditionalHealth, New Max Health: $finalMaxHealth (Base: $baseDragonHealth, Per Player Factor: $additionalHealthPerPlayer)")
        } ?: LOGGER.warn("Could not find GENERIC_MAX_HEALTH attribute for Ender Dragon. Health not scaled.")

        if (enableBroadcast) {
            broadcastScalingMessage(world, scaleEligiblePlayerCount, finalMaxHealth)
        }
    }

    private fun broadcastScalingMessage(world: ServerWorld, scaleEligiblePlayerCount: Int, finalMaxHealth: Float) {
        val server = world.server
        val broadcastMessageText =
            if (scaleEligiblePlayerCount > 0 && (scaleWithOnePlayer || scaleEligiblePlayerCount > 1)) {
                // Announce scaling if it happened and health is above vanilla default
                if (finalMaxHealth > VANILLA_DRAGON_HEALTH) {
                    "An ENDER DRAGON has appeared in THE END! Its power scales with $scaleEligiblePlayerCount brave warrior(s)!"
                } else {
                    "An ENDER DRAGON has appeared in THE END!"
                }
            } else {
                "An ENDER DRAGON has appeared in THE END!"
            }
        val message = Text.literal(broadcastMessageText)
        server.playerManager.broadcast(message, false)
    }
}