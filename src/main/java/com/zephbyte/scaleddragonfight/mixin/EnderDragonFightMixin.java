package com.zephbyte.scaleddragonfight.mixin;

import com.zephbyte.scaleddragonfight.DragonEventHandler;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(EnderDragonFight.class)
public abstract class EnderDragonFightMixin {

    @Shadow
    @Final
    private ServerWorld world;

    @Shadow
    public abstract boolean hasPreviouslyKilled();

    @Inject(
            method = "createDragon()Lnet/minecraft/entity/boss/dragon/EnderDragonEntity;", // Target createDragon()
            at = @At("HEAD"),
            cancellable = true
    )
    private void scaleddragonfight_onPreCreateDragon(CallbackInfoReturnable<EnderDragonEntity> cir) { // Updated parameter
        EnderDragonFight fightInstance = (EnderDragonFight) (Object) this;

        // We only want to delay the *very first* dragon spawn in this world.
        // hasPreviouslyKilled() is the best check for this.
        if (!this.hasPreviouslyKilled()) {
            // Call our handler. If it returns true, it means we are delaying the spawn.
            if (DragonEventHandler.INSTANCE.onInitialDragonPreSpawn(fightInstance, this.world)) {
                // If we are delaying, we cancel the original createDragon() method
                // and make it return null for now, as the dragon entity isn't created yet.
                cir.setReturnValue(null);
                cir.cancel();
            }
        }
        // If hasPreviouslyKilled() is true, or if onInitialDragonPreSpawn returns false
        // (e.g., feature disabled, or it's our mod triggering the spawn after the delay),
        // this injection does nothing, and createDragon() proceeds normally.
    }
}