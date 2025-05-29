package com.zephbyte.scaleddragonfight

import com.zephbyte.scaleddragonfight.ConfigManager.enableMod // Direct access to config
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World

object DragonEventHandler {

    fun register() {
        ServerEntityEvents.ENTITY_LOAD.register { entity, world ->
            // Check enableMod from ConfigManager
            if (!enableMod) {
                return@register // If mod is disabled, do nothing
            }

            if (world is ServerWorld && entity is EnderDragonEntity && world.registryKey == World.END) {
                LOGGER.info("Ender Dragon detected loading in The End! Checking scaling conditions...")
                // Delegate the actual scaling logic to DragonScaler
                DragonScaler.scaleDragon(entity, world)
            }
        }
    }
}