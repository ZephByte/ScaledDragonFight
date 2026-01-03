package com.zephbyte.scaleddragonfight.mixin;

import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List; // Import List

@Mixin(EndDragonFight.class)
public interface EnderDragonFightAccessor {
    /**
     * Allows calling the private 'respawnDragon(List<EndCrystal>)' method
     * in EndDragonFight.
     * The method name in the @Invoker annotation must match the target method name
     * as it appears after mappings (usually the deobfuscated name).
     */
    @Invoker("respawnDragon")
    void callRespawnDragon(List<EndCrystal> crystals);
}