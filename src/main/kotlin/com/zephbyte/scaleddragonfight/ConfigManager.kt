package com.zephbyte.scaleddragonfight

import com.electronwill.nightconfig.core.file.CommentedFileConfig
import com.electronwill.nightconfig.core.io.WritingMode
import net.fabricmc.loader.api.FabricLoader
import kotlin.properties.ReadOnlyProperty

private object ConfigManager {

    // The 'lateinit' keyword means we promise to initialize it before it's ever accessed.
    // The 'private set' ensures that only the ConfigManager itself can replace the config object.
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
        // A new ConfigData object is created, which reads all the latest values from the file.
        // This new object then replaces the old one.
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
        val enableMod by define(config, "general.enableMod", true, "Enable or disable the entire mod.")
        val scaleWithOnePlayer by define(config, "general.scaleWithOnePlayer", false, "If true, dragon's health will be scaled even if there is only one player in The End.")
        val countCreativeModePlayers by define(config, "general.countCreativeModePlayers", false, "If true, players in creative mode will be counted for health scaling.")
    }

    class HealthScaling(config: CommentedFileConfig) {
        val baseDragonHealth by define(config, "healthScaling.baseDragonHealth", 200.0f, "The base health of the Ender Dragon.")
        val additionalHealthPerPlayer by define(config, "healthScaling.additionalHealthPerPlayer", 100.0f, "How much health to add to the Ender Dragon for each additional player.")
    }

    class BroadcastMessages(config: CommentedFileConfig) {
        val enableBroadcast by define(config, "broadcastMessages.enableBroadcast", true, "Enable or disable broadcast messages (e.g., when the dragon's health is scaled).")
    }

    class Countdown(config: CommentedFileConfig) {
        // TODO Zeph: Use these new configurables
        val enableCountdownOverworld by define(config, "countdown.enableCountdownOverworld", true, "Show the countdown to players in the Overworld.")
        val enableCountdownTheEnd by define(config, "countdown.enableCountdownTheEnd", true, "Show the countdown to players in The End.")
        val enableCountdownNether by define(config, "countdown.enableCountdownNether", false, "Show the countdown to players in the Nether.")
    }

    class SpawnDelay(config: CommentedFileConfig) {
        val enableInitialSpawnDelay by define(config, "spawnDelay.enableInitialSpawnDelay", true, "Enable a delay before the Ender Dragon initially spawns.")
        val initialSpawnDelaySeconds by define(config, "spawnDelay.initialSpawnDelaySeconds", 60, "The duration of the initial spawn delay in seconds.")
        val showSpawnDelayCountdown by define(config, "spawnDelay.showSpawnDelayCountdown", true, "Show a countdown message for the initial spawn delay.")
    }
}


/**
 * A generic helper function that creates a delegated property for a config value.
 * This is now a top-level private function, accessible within this file.
 */
@Suppress("UNCHECKED_CAST")
private fun <T> define(config: CommentedFileConfig, path: String, defaultValue: T, comment: String): ReadOnlyProperty<Any?, T> {
    config.setComment(path, " [default: $defaultValue]\n $comment")

    // Use getOrElse to safely get the value. If it's missing or the wrong type,
    // it will set the path to the default value and return that.
    val value = config.getOrElse(path, defaultValue)
    if (!config.contains(path)) {
        config.set<T>(path, defaultValue)
    }

    // Return a property delegate that simply provides the value loaded at creation time.
    return ReadOnlyProperty { _, _ -> value as T }
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