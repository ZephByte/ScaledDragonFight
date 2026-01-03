package com.zephbyte.scaleddragonfight

// Import specific config values needed
import com.zephbyte.scaleddragonfight.ConfigManager.additionalHealthPerPlayer
import com.zephbyte.scaleddragonfight.ConfigManager.baseDragonHealth
import com.zephbyte.scaleddragonfight.ConfigManager.countCreativeModePlayers
import com.zephbyte.scaleddragonfight.ConfigManager.enableBroadcast
import com.zephbyte.scaleddragonfight.ConfigManager.scaleWithOnePlayer
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.chat.Component

object DragonScaler {

    private const val VANILLA_DRAGON_HEALTH = 200.0f // Vanilla default for comparison

    fun scaleDragon(dragon: EnderDragon, level: ServerLevel) {
        // Filter players in The End and consider creative mode setting
        val presentPlayers = level.players().filterIsInstance<ServerPlayer>()
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

        dragon.getAttribute(Attributes.MAX_HEALTH)?.let { attributeInstance ->
            attributeInstance.baseValue = finalMaxHealth.toDouble()
            dragon.health = finalMaxHealth // Set current health to new max health
            LOGGER.info("Scaled Ender Dragon health. Eligible Players: $scaleEligiblePlayerCount, Players Adding Health: $playersContributingToAdditionalHealth, New Max Health: $finalMaxHealth (Base: $baseDragonHealth, Per Player Factor: $additionalHealthPerPlayer)")
        } ?: LOGGER.warn("Could not find GENERIC_MAX_HEALTH attribute for Ender Dragon. Health not scaled.")

        if (enableBroadcast) {
            broadcastScalingMessage(level, scaleEligiblePlayerCount, finalMaxHealth)
        }
    }

    private fun broadcastScalingMessage(level: ServerLevel, scaleEligiblePlayerCount: Int, finalMaxHealth: Float) {
        val server = level.server
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
        val message = Component.literal(broadcastMessageText)
        server.playerList.broadcastSystemMessage(message, false)
    }
}