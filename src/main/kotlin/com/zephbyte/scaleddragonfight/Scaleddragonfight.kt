package com.zephbyte.scaleddragonfight

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.world.World
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Scaleddragonfight : ModInitializer {

    companion object {
        // Making LOGGER non-nullable for cleaner code and direct access
        val LOGGER: Logger = LoggerFactory.getLogger("scaleddragonfight")

        // Configuration for health scaling
        // Default Ender Dragon health is 200.0f
        const val BASE_DRAGON_HEALTH = 200.0f
        // Amount of extra health to add for each player in The End
        const val ADDITIONAL_HEALTH_PER_PLAYER = 100.0f // Example: 100 extra HP per player
    }

    override fun onInitialize() {
        LOGGER.info("Scaled Dragon Fight mod initializing")

        ServerEntityEvents.ENTITY_LOAD.register { entity, world ->
            if (world is ServerWorld && entity is EnderDragonEntity && world.registryKey == World.END) {
                val dragon = entity // Smart cast to EnderDragonEntity
                LOGGER.info("Ender Dragon detected loading in The End!")

                // Get the number of players currently in The End dimension
                // world.players gives a list of ServerPlayerEntity in that specific world instance
                val playerCount = world.players.size

                // Calculate the new maximum health
                val newMaxHealth = BASE_DRAGON_HEALTH + if (playerCount > 1) { ADDITIONAL_HEALTH_PER_PLAYER * (playerCount - 1) } else 0.0f

                // Get the max health attribute instance and set its base value
                dragon.getAttributeInstance(EntityAttributes.MAX_HEALTH)?.let { attributeInstance ->
                    attributeInstance.baseValue = newMaxHealth.toDouble()
                    // Set the dragon's current health to its new maximum health
                    dragon.health = newMaxHealth // dragon.health is a Float
                    LOGGER.info("Scaled Ender Dragon health. Players: $playerCount, New Max Health: $newMaxHealth (Base: $BASE_DRAGON_HEALTH, Per Additional Player: $ADDITIONAL_HEALTH_PER_PLAYER)")
                } ?: LOGGER.warn("Could not find GENERIC_MAX_HEALTH attribute for Ender Dragon. Health not scaled.")


                // Broadcast a message to all players
                val server = world.server
                val broadcastMessageText = if (playerCount > 1) {
                    "An ENDER DRAGON has appeared in THE END! Its power scales with $playerCount brave warriors!"
                } else {
                    "An ENDER DRAGON has appeared in THE END!"
                }
                val message = Text.literal(broadcastMessageText)
                // Using 'false' for a system message (often preferred for boss spawns)
                server.playerManager.broadcast(message, false)
            }
        }
    }
}
