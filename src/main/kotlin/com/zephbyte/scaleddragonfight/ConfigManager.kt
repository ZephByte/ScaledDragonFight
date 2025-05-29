package com.zephbyte.scaleddragonfight

import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

object ConfigManager {

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

                LOGGER.info("Configuration loaded: Mod Enabled = $enableMod, Scale w/ 1 Player = $scaleWithOnePlayer, Count Creative = $countCreativeModePlayers, Base Health = $baseDragonHealth, Additional Health/Player = $additionalHealthPerPlayer, Enable Broadcast = $enableBroadcast")
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
            scaleWithOnePlayer: If true, the dragon's health will increase counting the first eligible player.
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
        enableBroadcast = DEFAULT_ENABLE_BROADCAST
        saveConfig()
    }
}