package com.zephbyte.scaleddragonfight

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
                if (!updateNotificationSent && handler.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                    val changelogUrl = URI("https://modrinth.com/mod/scaled-dragon-fight/version/v1.0.1+1.21.10") // TODO Zeph: Update this link when releasing
                    val changelogText = Component.literal("[Changelog for v2.0.0]")
                        .setStyle(
                            Style.EMPTY
                                .withClickEvent(ClickEvent.OpenUrl(changelogUrl))
                                .withHoverEvent(HoverEvent.ShowText(Component.literal("Click to open changelog")))
                                .withColor(ChatFormatting.BLUE)
                                .withUnderlined(true).withItalic(true)
                        )

                    val reloadCommandText = Component.literal("/scaleddragonfight reload")
                        .setStyle(
                            Style.EMPTY
                                .withColor(ChatFormatting.AQUA)
                                .withClickEvent(ClickEvent.SuggestCommand("/scaleddragonfight reload"))
                                .withHoverEvent(HoverEvent.ShowText(Component.literal("Click to suggest command")))
                        )

                    val fullMessage = Component.literal("")
                        .append(Component.literal("IMPORTANT CONFIG UPDATE\n").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                        .append(Component.literal("Scaled Dragon Fight").withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(" ALL CONFIG SETTINGS have been reset to default for v2.0.0.\n").withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal("Your old settings were saved to ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("config/scaleddragonfight.properties.bak\n").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal("After migrating settings, run ").withStyle(ChatFormatting.GRAY))
                        .append(reloadCommandText)
                        .append(Component.literal(" to apply them without a restart.\n").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("See the ").withStyle(ChatFormatting.GRAY))
                        .append(changelogText)
                        .append(Component.literal(" for more info.").withStyle(ChatFormatting.GRAY))

                    handler.player.sendSystemMessage(fullMessage)
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