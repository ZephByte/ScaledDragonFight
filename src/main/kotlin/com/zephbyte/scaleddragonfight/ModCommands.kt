package com.zephbyte.scaleddragonfight

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object ModCommands {

    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            registerReloadCommand(dispatcher)
        }
    }

    private fun registerReloadCommand(
        dispatcher: CommandDispatcher<ServerCommandSource>
    ) {
        dispatcher.register(
            CommandManager.literal(MOD_ID)
                .then(CommandManager.literal("reload")
                    .requires { source -> source.hasPermissionLevel(2) } // Only OPs (level 2) can reload
                    .executes { context ->
                        SDFConfig.reload() // Reload config from ConfigManager
                        val feedbackMsg = if (SDFConfig.general.enableMod) { // Check enableMod from ConfigManager
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
}