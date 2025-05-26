package com.zephbyte.scaleddragonfight

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.text.Text
import net.minecraft.world.World
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Scaleddragonfight : ModInitializer {

    companion object {
        val LOGGER: Logger? = LoggerFactory.getLogger("scaleddragonfight")
    }

    override fun onInitialize() {
        LOGGER?.info("Scaled Dragon Fight mod initializing")

        ServerEntityEvents.ENTITY_LOAD.register { entity, world ->
            if ((entity is EnderDragonEntity) && (world.registryKey == World.END)) {
                LOGGER?.info("Ender Dragon detected in The End!")

                val server = world.server
                val message = Text.literal("An ENDER DRAGON has appeared in THE END!")

                server.playerManager.broadcast(message, true)
            }
        }
    }
}
