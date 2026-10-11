package com.zephbyte.scaleddragonfight

import com.electronwill.nightconfig.core.file.CommentedFileConfig
import com.electronwill.nightconfig.core.io.WritingMode
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.server.permissions.Permissions
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

// TODO Zeph: Remove this legacy config check in v3.0.0
// This code exists to help users migrate from v1.x.x `.properties` config
// to the v2.x.x `.toml` config. By v3.0.0 we can assume most have migrated.
object LegacyConfigMigrator {

    private var updateNotificationSent = false

    /**
     * Imports a v1.x `.properties` config into the v2.x `.toml` config, then renames the
     * old file to `.properties.bak`. Must run BEFORE [SDFConfig.reload], which fills in
     * comments and defaults for anything that was not imported.
     */
    fun runCheck() {
        val configDir = FabricLoader.getInstance().configDir
        val legacyConfigFile = configDir.resolve("$MOD_ID.properties")
        if (!Files.exists(legacyConfigFile)) return

        val tomlFile = configDir.resolve("$MOD_ID.toml")
        // Never overwrite a TOML config that already exists, the user may have edited it.
        val imported = if (Files.exists(tomlFile)) {
            LOGGER.warn("Found legacy '$MOD_ID.properties' but '$MOD_ID.toml' already exists. Not importing.")
            0
        } else {
            importLegacySettings(legacyConfigFile, tomlFile)
        }

        LOGGER.warn("=====================================================================================")
        LOGGER.warn("LEGACY CONFIG DETECTED! Scaled Dragon Fight has updated to a new '.toml' format.")
        LOGGER.warn("Imported $imported setting(s) from 'scaleddragonfight.properties' into 'scaleddragonfight.toml'.")
        LOGGER.warn("The old file was kept as 'scaleddragonfight.properties.bak'. Please review the new file.")
        LOGGER.warn("=====================================================================================")

        // Register an event to notify the first OP that joins
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            if (!updateNotificationSent && handler.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                handler.player.sendSystemMessage(buildNotification(imported))
                updateNotificationSent = true // Ensure we only send this once per server start
            }
        }

        // Rename the old file to .bak to prevent confusion and preserve user's settings
        try {
            Files.move(legacyConfigFile, configDir.resolve("$MOD_ID.properties.bak"))
            LOGGER.info("Renamed legacy config to 'scaleddragonfight.properties.bak'.")
        } catch (e: Exception) {
            LOGGER.error("Failed to rename legacy config file.", e)
        }
    }

    /** Writes the recognised legacy values into a new TOML file. Returns how many were imported. */
    private fun importLegacySettings(legacyFile: Path, tomlFile: Path): Int {
        val props = Properties()
        try {
            Files.newInputStream(legacyFile).use { props.load(it) }
        } catch (e: Exception) {
            LOGGER.error("Failed to read legacy config. Defaults will be used.", e)
            return 0
        }

        // legacy key -> (new TOML path, parser). Parsers return null for invalid values, which are skipped.
        val bool: (String) -> Any? = { it.trim().toBooleanStrictOrNull() }
        val float: (String) -> Any? = { it.trim().toFloatOrNull() }
        val int: (String) -> Any? = { it.trim().toIntOrNull() }
        val mapping = listOf(
            Triple("enableMod", "general.enableMod", bool),
            Triple("scaleWithOnePlayer", "general.scaleWithOnePlayer", bool),
            Triple("countCreativeModePlayers", "general.countCreativeModePlayers", bool),
            Triple("baseDragonHealth", "healthScaling.baseDragonHealth", float),
            Triple("additionalHealthPerPlayer", "healthScaling.additionalHealthPerPlayer", float),
            Triple("enableBroadcast", "broadcastMessages.enableBroadcast", bool),
            Triple("enableInitialSpawnDelay", "spawnDelay.enableInitialSpawnDelay", bool),
            Triple("initialSpawnDelaySeconds", "spawnDelay.initialSpawnDelaySeconds", int),
        )

        var count = 0
        try {
            // sync(): the default config is async, and SDFConfig.reload() reads this file right after.
            CommentedFileConfig.builder(tomlFile).sync().writingMode(WritingMode.REPLACE).build().use { toml ->
                for ((oldKey, newPath, parse) in mapping) {
                    val value = props.getProperty(oldKey)?.let(parse) ?: continue
                    toml.set<Any>(newPath, value)
                    count++
                }

                // v1 had one countdown toggle (shown in The End only). v2 has one per dimension.
                // If the old one was turned off, keep the countdown off everywhere.
                if (props.getProperty("showSpawnDelayCountdown")?.trim()?.toBooleanStrictOrNull() == false) {
                    toml.set<Any>("countdown.enableCountdownTheEnd", false)
                    toml.set<Any>("countdown.enableCountdownOverworld", false)
                    toml.set<Any>("countdown.enableCountdownNether", false)
                    count++
                }
                toml.save()
            }
        } catch (e: Exception) {
            LOGGER.error("Failed to import legacy config. Defaults will be used.", e)
            return 0
        }
        return count
    }

    private fun buildNotification(importedCount: Int): Component {
        val changelogUrl = URI("https://modrinth.com/mod/scaled-dragon-fight/version/v2.0.0+26.1")
        val changelogText = Component.literal("[Changelog for v2.0.0]")
            .setStyle(
                Style.EMPTY
                    .withClickEvent(ClickEvent.OpenUrl(changelogUrl))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal("Click to open changelog")))
                    .withColor(ChatFormatting.BLUE)
                    .withUnderlined(true)
                    .withItalic(true)
            )

        val reloadCommandText = Component.literal("/scaleddragonfight reload")
            .setStyle(
                Style.EMPTY
                    .withColor(ChatFormatting.AQUA)
                    .withClickEvent(ClickEvent.SuggestCommand("/scaleddragonfight reload"))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal("Click to suggest command")))
            )

        val result = if (importedCount > 0) {
            "Your old settings ($importedCount) were imported into config/scaleddragonfight.toml.\n"
        } else {
            "Your old settings could not be imported, so defaults are in use.\n"
        }

        return Component.literal("")
            .append(Component.literal("IMPORTANT CONFIG UPDATE\n").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
            .append(Component.literal("Scaled Dragon Fight").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" now uses a new .toml config for v2.0.0.\n").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(result).withStyle(ChatFormatting.GRAY))
            .append(Component.literal("The original was saved to ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("config/scaleddragonfight.properties.bak\n").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("After reviewing the new file, run ").withStyle(ChatFormatting.GRAY))
            .append(reloadCommandText)
            .append(Component.literal(" to apply changes without a restart.\n").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("See the ").withStyle(ChatFormatting.GRAY))
            .append(changelogText)
            .append(Component.literal(" for more info.").withStyle(ChatFormatting.GRAY))
    }
}
