package com.zephbyte.scaleddragonfight

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.net.URI
import java.nio.file.Files

// TODO Zeph: Remove this legacy config check in v3.0.0
// This code exists to help users migrate from v1.x.x `.properties` config
// to the v2.x.x `.toml` config. By v3.0.0 we can assume most have migrated.
object LegacyConfigMigrator {

    private var updateNotificationSent = false

    fun runCheck() {
        val configDir = FabricLoader.getInstance().configDir
        val legacyConfigFile = configDir.resolve("$MOD_ID.properties")

        if (Files.exists(legacyConfigFile)) {
            // Log a big warning to the server console
            LOGGER.warn("=====================================================================================")
            LOGGER.warn("LEGACY CONFIG DETECTED! Scaled Dragon Fight has updated to a new '.toml' format.")
            LOGGER.warn("Your old 'scaleddragonfight.properties' file is no longer used.")
            LOGGER.warn("Your settings must be manually migrated to the new 'scaleddragonfight.toml' file.")
            LOGGER.warn("=====================================================================================")

            // Register an event to notify the first OP that joins
            ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
                if (!updateNotificationSent && handler.player.hasPermissionLevel(2)) {
                    val changelogUrl = URI("https://modrinth.com/mod/scaled-dragon-fight/version/v1.0.1+1.21.10") // TODO Zeph: Update this link when releasing
                    val changelogText = Text.literal("[Changelog for v2.0.0]")
                        .setStyle(
                            Style.EMPTY
                                .withClickEvent(ClickEvent.OpenUrl(changelogUrl))
                                .withHoverEvent(HoverEvent.ShowText(Text.literal("Click to open changelog")))
                                .withColor(Formatting.BLUE)
                                .withFormatting(Formatting.UNDERLINE, Formatting.ITALIC)
                        )

                    val reloadCommandText = Text.literal("/scaleddragonfight reload")
                        .setStyle(
                            Style.EMPTY
                                .withColor(Formatting.AQUA)
                                .withClickEvent(ClickEvent.SuggestCommand("/scaleddragonfight reload"))
                                .withHoverEvent(HoverEvent.ShowText(Text.literal("Click to suggest command")))
                        )

                    val fullMessage = Text.literal("")
                        .append(Text.literal("IMPORTANT CONFIG UPDATE\n").formatted(Formatting.RED, Formatting.BOLD))
                        .append(Text.literal("Scaled Dragon Fight").formatted(Formatting.GOLD))
                        .append(Text.literal(" ALL CONFIG SETTINGS have been reset to default for v2.0.0.\n").formatted(Formatting.YELLOW))
                        .append(Text.literal("Your old settings were saved to ").formatted(Formatting.GRAY))
                        .append(Text.literal("config/scaleddragonfight.properties.bak\n").formatted(Formatting.AQUA))
                        .append(Text.literal("After migrating settings, run ").formatted(Formatting.GRAY))
                        .append(reloadCommandText)
                        .append(Text.literal(" to apply them without a restart.\n").formatted(Formatting.GRAY))
                        .append(Text.literal("See the ").formatted(Formatting.GRAY))
                        .append(changelogText)
                        .append(Text.literal(" for more info.").formatted(Formatting.GRAY))

                    handler.player.sendMessage(fullMessage, false)
                    updateNotificationSent = true // Ensure we only send this once per server start
                }
            }

            // Rename the old file to .bak to prevent confusion and preserve user's settings
            try {
                val backupFile = configDir.resolve("$MOD_ID.properties.bak")
                Files.move(legacyConfigFile, backupFile)
                LOGGER.info("Renamed legacy config to 'scaleddragonfight.properties.bak'.")
            } catch (e: Exception) {
                LOGGER.error("Failed to rename legacy config file.", e)
            }
        }
    }
}