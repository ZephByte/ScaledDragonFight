package com.zephbyte.scaleddragonfight

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.server.permissions.Permissions

object ModCommands {

    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            registerReloadCommand(dispatcher)
        }
    }

    private fun registerReloadCommand(
        dispatcher: CommandDispatcher<CommandSourceStack>
    ) {
        dispatcher.register(
            Commands.literal(MOD_ID)
                .then(Commands.literal("reload")
                    .requires { source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN) } // Only ADMINS can reload
                    .executes { context ->
                        SDFConfig.reload()
                        val feedbackMsg = if (SDFConfig.general.enableMod) {
                            "Scaled Dragon Fight configuration reloaded. Mod is ENABLED."
                        } else {
                            "Scaled Dragon Fight configuration reloaded. Mod is DISABLED."
                        }
                        context.source.sendSuccess({ Component.literal(feedbackMsg) }, true)
                        1 // Return 1 for success
                    }
                )
        )
    }
}