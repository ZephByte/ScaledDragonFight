package com.zephbyte.scaleddragonfight

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.world.World
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

class Scaleddragonfight : ModInitializer {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger("scaleddragonfight")
        const val MOD_ID = "scaleddragonfight"

        // Default configuration values
        private const val DEFAULT_ENABLE_MOD = true
        private const val DEFAULT_SCALE_WITH_ONE_PLAYER = false
        private const val DEFAULT_COUNT_CREATIVE_MODE_PLAYERS = false
        private const val DEFAULT_BASE_DRAGON_HEALTH = 200.0f
        private const val DEFAULT_ADDITIONAL_HEALTH_PER_PLAYER = 100.0f
        private const val DEFAULT_ENABLE_BROADCAST = true

        // Configurable values
        var enableMod: Boolean = DEFAULT_ENABLE_MOD
        var scaleWithOnePlayer: Boolean = DEFAULT_SCALE_WITH_ONE_PLAYER
        var countCreativeModePlayers: Boolean = DEFAULT_COUNT_CREATIVE_MODE_PLAYERS
        var baseDragonHealth: Float = DEFAULT_BASE_DRAGON_HEALTH
        var additionalHealthPerPlayer: Float = DEFAULT_ADDITIONAL_HEALTH_PER_PLAYER
        var enableBroadcast: Boolean = DEFAULT_ENABLE_BROADCAST

        private val configFilePath: Path = FabricLoader.getInstance().configDir.resolve("$MOD_ID.properties")

        fun loadConfig() {
            LOGGER.info("Loading Scaled Dragon Fight configuration...")
            val properties = Properties()

            if (Files.exists(configFilePath)) {
                try {
                    Files.newInputStream(configFilePath).use { inputStream ->
                        properties.load(inputStream)
                    }

                    enableMod = properties.getProperty("enableMod", DEFAULT_ENABLE_MOD.toString()).toBooleanStrictOrNull() ?: DEFAULT_ENABLE_MOD
                    scaleWithOnePlayer = properties.getProperty("scaleWithOnePlayer", DEFAULT_SCALE_WITH_ONE_PLAYER.toString()).toBooleanStrictOrNull() ?: DEFAULT_SCALE_WITH_ONE_PLAYER
                    countCreativeModePlayers = properties.getProperty("countCreativeModePlayers", DEFAULT_COUNT_CREATIVE_MODE_PLAYERS.toString()).toBooleanStrictOrNull() ?: DEFAULT_COUNT_CREATIVE_MODE_PLAYERS
                    baseDragonHealth = properties.getProperty("baseDragonHealth", DEFAULT_BASE_DRAGON_HEALTH.toString())
                        .toFloatOrNull() ?: DEFAULT_BASE_DRAGON_HEALTH
                    additionalHealthPerPlayer = properties.getProperty("additionalHealthPerPlayer", DEFAULT_ADDITIONAL_HEALTH_PER_PLAYER.toString())
                        .toFloatOrNull() ?: DEFAULT_ADDITIONAL_HEALTH_PER_PLAYER
                    enableBroadcast = properties.getProperty("enableBroadcast", DEFAULT_ENABLE_BROADCAST.toString()).toBooleanStrictOrNull() ?: DEFAULT_ENABLE_BROADCAST

                    LOGGER.info("Configuration loaded: Mod Enabled = $enableMod, Scale w/ 1 Player = $scaleWithOnePlayer, Count Creative = $countCreativeModePlayers, Base Health = $baseDragonHealth, Additional Health/Player = $additionalHealthPerPlayer")
                    // Ensure config file is up-to-date with current or default values if parsing failed for some
                    saveConfig()
                } catch (e: Exception) {
                    LOGGER.error("Failed to load configuration for $MOD_ID. Using default values and attempting to save a new config file.", e)
                    resetToDefaultsAndSave()
                }
            } else {
                LOGGER.info("No configuration file found for $MOD_ID. Creating with default values.")
                resetToDefaultsAndSave()
            }
        }

        fun saveConfig() {
            LOGGER.info("Saving Scaled Dragon Fight configuration...")
            val properties = Properties()

            properties.setProperty("enableMod", enableMod.toString())
            properties.setProperty("scaleWithOnePlayer", scaleWithOnePlayer.toString())
            properties.setProperty("countCreativeModePlayers", countCreativeModePlayers.toString())
            properties.setProperty("baseDragonHealth", baseDragonHealth.toString())
            properties.setProperty("additionalHealthPerPlayer", additionalHealthPerPlayer.toString())
            properties.setProperty("enableBroadcast", enableBroadcast.toString())

            val comments = """
                Scaled Dragon Fight Configuration
                
                enableMod: If true, the mod will be active. (Default: $DEFAULT_ENABLE_MOD)
                baseDragonHealth: Base health of the Ender Dragon. (Default: $DEFAULT_BASE_DRAGON_HEALTH)
                additionalHealthPerPlayer: Extra health added for each eligible player. (Default: $DEFAULT_ADDITIONAL_HEALTH_PER_PLAYER)
                scaleFirstPlayer: If true, the dragon's health will increase counting the first eligible player.
                                  If false, scaling only starts with the second eligible player.
                                  Example (assuming 100 additional health per player):
                                      True:
                                          1 Eligible Player = Base Health + 100
                                          2 Eligible Players = Base Health + 200
                                      False:
                                          1 Eligible Player = Base Health
                                          2 Eligible Players = Base Health + 100
                                  (Default: $DEFAULT_SCALE_WITH_ONE_PLAYER)
                countCreativeModePlayers: If true, players in creative mode will be counted when scaling health. (Default: $DEFAULT_COUNT_CREATIVE_MODE_PLAYERS)
                enableBroadcast: If true, a message will be broadcast when the scaled dragon spawns. (Default: $DEFAULT_ENABLE_BROADCAST)
            """.trimIndent()

            try {
                Files.newOutputStream(configFilePath).use { outputStream ->
                    properties.store(outputStream, comments)
                }
                LOGGER.info("Configuration saved to $configFilePath")
            } catch (e: Exception) {
                LOGGER.error("Failed to save configuration for $MOD_ID.", e)
            }
        }

        private fun resetToDefaultsAndSave() {
            enableMod = DEFAULT_ENABLE_MOD
            scaleWithOnePlayer = DEFAULT_SCALE_WITH_ONE_PLAYER
            countCreativeModePlayers = DEFAULT_COUNT_CREATIVE_MODE_PLAYERS
            baseDragonHealth = DEFAULT_BASE_DRAGON_HEALTH
            additionalHealthPerPlayer = DEFAULT_ADDITIONAL_HEALTH_PER_PLAYER
            saveConfig()
        }
    }

    override fun onInitialize() {
        LOGGER.info("Scaled Dragon Fight mod initializing...")
        loadConfig() // Load configuration on mod startup

        ServerEntityEvents.ENTITY_LOAD.register { entity, world ->
            // CRITICAL CHANGE: Check enableMod INSIDE the event listener
            if (!enableMod) {
                return@register // If mod is disabled, do nothing
            }

            if (world is ServerWorld && entity is EnderDragonEntity && world.registryKey == World.END) {
                val dragon = entity // Smart cast
                LOGGER.info("Ender Dragon detected loading in The End! Checking scaling conditions...")

                // Filter players in The End and consider creative mode setting
                val presentPlayers = world.players.filterIsInstance<ServerPlayerEntity>()
                val eligiblePlayers = presentPlayers.filter { player ->
                    countCreativeModePlayers || !player.isCreative
                }
                val scaleEligiblePlayerCount = eligiblePlayers.size

                // Determine the number of players that contribute to *additional* health
                val playersContributingToAdditionalHealth = when {
                    !scaleWithOnePlayer && scaleEligiblePlayerCount <= 1 -> 0 // No additional health if not scaling with one and only 0-1 players
                    scaleWithOnePlayer && scaleEligiblePlayerCount >= 1 -> scaleEligiblePlayerCount // All eligible players contribute if scaling with one
                    else -> scaleEligiblePlayerCount - 1 // Otherwise, all but the first player contribute
                }

                val newMaxHealth = baseDragonHealth + (additionalHealthPerPlayer * playersContributingToAdditionalHealth)

                // Ensure health doesn't go below the configured base or a reasonable minimum
                val finalMaxHealth = newMaxHealth.coerceAtLeast(baseDragonHealth)


                dragon.getAttributeInstance(EntityAttributes.MAX_HEALTH)?.let { attributeInstance ->
                    attributeInstance.baseValue = finalMaxHealth.toDouble()
                    dragon.health = finalMaxHealth // Set current health to new max health
                    LOGGER.info("Scaled Ender Dragon health. Eligible Players: $scaleEligiblePlayerCount, Players Adding Health: $playersContributingToAdditionalHealth, New Max Health: $finalMaxHealth (Base: $baseDragonHealth, Per Player Factor: $additionalHealthPerPlayer)")
                } ?: LOGGER.warn("Could not find GENERIC_MAX_HEALTH attribute for Ender Dragon. Health not scaled.")

                if (enableBroadcast) {
                    val server = world.server
                    val broadcastMessageText =
                        if (scaleEligiblePlayerCount > 0 && (scaleWithOnePlayer || scaleEligiblePlayerCount > 1)) {
                            // Only announce scaling if it actually happened based on player count and settings
                            if (finalMaxHealth > DEFAULT_BASE_DRAGON_HEALTH) { // Or compare to vanilla default if you prefer
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
        }

        // Register the reload command
        CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment ->
            dispatcher.register(
                literal(MOD_ID)
                    .then(literal("reload")
                        .requires { source -> source.hasPermissionLevel(2) } // Only OPs (level 2) can reload
                        .executes { context ->
                            loadConfig()
                            val feedbackMsg = if (enableMod) {
                                "Scaled Dragon Fight configuration reloaded. Mod is ENABLED."
                            } else {
                                "Scaled Dragon Fight configuration reloaded. Mod is DISABLED."
                            }
                            context.source.sendFeedback({ Text.literal(feedbackMsg) }, true)
                            1 // Return 1 for success
                        }
                    )
            )
        }
        LOGGER.info("Scaled Dragon Fight mod initialized. Event listener and reload command registered.")
    }
}