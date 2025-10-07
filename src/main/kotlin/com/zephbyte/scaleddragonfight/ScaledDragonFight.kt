package com.zephbyte.scaleddragonfight

import net.fabricmc.api.ModInitializer

class ScaledDragonFight : ModInitializer {

    override fun onInitialize() {
        LOGGER.info("Scaled Dragon Fight mod initializing...")

        SDFConfig.reload()
        DragonEventHandler.register()
        ModCommands.register()

        LOGGER.info("Scaled Dragon Fight mod initialized. Event listener and reload command registered.")
    }
}