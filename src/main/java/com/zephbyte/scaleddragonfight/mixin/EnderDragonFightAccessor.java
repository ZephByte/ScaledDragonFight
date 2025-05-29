package com.zephbyte.scaleddragonfight.mixin;

import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.decoration.EndCrystalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List; // Import List

@Mixin(EnderDragonFight.class)
public interface EnderDragonFightAccessor {
    /**
     * Allows calling the private 'respawnDragon(List<EndCrystalEntity>)' method
     * in EnderDragonFight.
     * The method name in the @Invoker annotation must match the target method name
     * as it appears after mappings (usually the deobfuscated name).
     */
    @Invoker("respawnDragon")
    void callRespawnDragon(List<EndCrystalEntity> crystals);
}