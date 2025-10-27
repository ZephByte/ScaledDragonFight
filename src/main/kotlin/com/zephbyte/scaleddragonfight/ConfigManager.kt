package com.zephbyte.scaleddragonfight

import com.electronwill.nightconfig.core.file.CommentedFileConfig
import com.electronwill.nightconfig.core.io.WritingMode
import net.fabricmc.loader.api.FabricLoader
import kotlin.properties.ReadOnlyProperty

private object ConfigManager {

    lateinit var config: ConfigData
        private set

    private val builder = CommentedFileConfig.builder(FabricLoader.getInstance().configDir.resolve("$MOD_ID.toml"))
        .autosave()
        .writingMode(WritingMode.REPLACE)

    /**
     * Loads or reloads the configuration from the file on disk.
     * This function is called by the public-facing SDFConfig object.
     */
    fun reloadConfig() {
        val fileConfig = builder.build()
        fileConfig.load()
        config = ConfigData(fileConfig)
        fileConfig.save()
        fileConfig.close()
    }
}

/**
 * A data class to hold all configuration values in a structured way.
 * This is a public class so that its properties can be exposed by the public SDFConfig object.
 */
class ConfigData(fileConfig: CommentedFileConfig) {
    val general = General(fileConfig)
    val healthScaling = HealthScaling(fileConfig)
    val broadcastMessages = BroadcastMessages(fileConfig)
    val countdown = Countdown(fileConfig)
    val spawnDelay = SpawnDelay(fileConfig)

    class General(config: CommentedFileConfig) {
        val enableMod by define(
            config,
            "general.enableMod",
            true,
            "Enable or disable the entire mod.")
        val scaleWithOnePlayer by define(
            config,
            "general.scaleWithOnePlayer",
            false,
            "If true, dragon's health will be scaled even if there is only one player in The End.")
        val countCreativeModePlayers by define(
            config,
            "general.countCreativeModePlayers",
            false,
            "If true, players in creative mode will be counted for health scaling.")
    }

    class HealthScaling(config: CommentedFileConfig) {
        val baseDragonHealth by define(
            config,
            "healthScaling.baseDragonHealth",
            200.0f,
            "The base health of the Ender Dragon.")
        val additionalHealthPerPlayer by define(
            config,
            "healthScaling.additionalHealthPerPlayer",
            100.0f,
            "How much health to add to the Ender Dragon for each additional player.")
    }

    class BroadcastMessages(config: CommentedFileConfig) {
        val enableBroadcast by define(
            config,
            "broadcastMessages.enableBroadcast",
            true,
            "Enable or disable broadcast messages (e.g., when the dragon's health is scaled).")
    }

    class Countdown(config: CommentedFileConfig) {
        val enableCountdownOverworld by define(
            config,
            "countdown.enableCountdownOverworld",
            true,
            "Show the countdown to players in the Overworld.")
        val enableCountdownTheEnd by define(
            config,
            "countdown.enableCountdownTheEnd",
            true,
            "Show the countdown to players in The End.")
        val enableCountdownNether by define(
            config,
            "countdown.enableCountdownNether",
            false,
            "Show the countdown to players in the Nether.")
    }

    class SpawnDelay(config: CommentedFileConfig) {
        val enableInitialSpawnDelay by define(
            config,
            "spawnDelay.enableInitialSpawnDelay",
            true,
            "Enable a delay before the Ender Dragon initially spawns.")
        val initialSpawnDelaySeconds by define(
            config,
            "spawnDelay.initialSpawnDelaySeconds",
            60,
            "The duration of the initial spawn delay in seconds.")
    }
}

/**
 * A generic helper function that creates a delegated property for a config value.
 * This is now a top-level private function, accessible within this file.
 */
@Suppress("UNCHECKED_CAST")
private fun <T : Any> define(config: CommentedFileConfig, path: String, defaultValue: T, comment: String): ReadOnlyProperty<Any?, T> {
    // Set the comment. This will be written to the file when config.save() is called.
    config.setComment(path, " [Default: $defaultValue]\n $comment")

    // Get the raw value from the config, which could be of any type.
    val rawValue = config.get<Any>(path)
    val value: T

    when {
        // Case 1: The value from the config is null or missing.
        rawValue == null -> {
            config.set<T>(path, defaultValue)
            value = defaultValue
        }

        // Case 2: We want a Float but got a Number (like a Double) that can be converted.
        defaultValue is Float && rawValue is Number -> {
            value = rawValue.toFloat() as T
        }

        // Case 3: The value exists and is already the correct type.
        defaultValue::class.java.isInstance(rawValue) -> {
            value = rawValue as T
        }

        // Case 4: The value is the wrong type and can't be handled automatically.
        else -> {
            config.set<T>(path, defaultValue)
            value = defaultValue
            LOGGER.info("ERROR: $path was set to $rawValue, which is NOT of type ${defaultValue::class.java}! Setting to default")
        }
    }

    // Return a property delegate that simply provides the value loaded at creation time.
    return ReadOnlyProperty { _, _ -> value }
}

/**
 * The public-facing API for accessing configuration values.
 *
 * To reload the config, call `SDFConfig.reload()`.
 * To access a value, use `SDFConfig.general.enableMod`.
 */
object SDFConfig {
    val general get() = ConfigManager.config.general
    val healthScaling get() = ConfigManager.config.healthScaling
    val broadcastMessages get() = ConfigManager.config.broadcastMessages
    val countdown get() = ConfigManager.config.countdown
    val spawnDelay get() = ConfigManager.config.spawnDelay

    /**
     * Public function to trigger a reload of the configuration from the file.
     */
    fun reload() {
        ConfigManager.reloadConfig()
    }
}