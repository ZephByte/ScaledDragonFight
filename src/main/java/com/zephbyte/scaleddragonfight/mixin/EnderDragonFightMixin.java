package com.zephbyte.scaleddragonfight.mixin;

import com.zephbyte.scaleddragonfight.DragonEventHandler;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.minecraft.server.level.ServerLevel;
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
    private ServerLevel level;

    @Shadow
    public abstract boolean hasPreviouslyKilledDragon();

    @Inject(
            method = "Lnet/minecraft/world/level/dimension/end/EnderDragonFight;createNewDragon()Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;", // Target createDragon()
            at = @At("HEAD"),
            cancellable = true
    )
    private void scaleddragonfight_onPreCreateDragon(CallbackInfoReturnable<EnderDragon> cir) { // Updated parameter
        EnderDragonFight fightInstance = (EnderDragonFight) (Object) this;

        // We only want to delay the *very first* dragon spawn in this world.
        // hasPreviouslyKilled() is the best check for this.
        if (!this.hasPreviouslyKilledDragon()) {
            // Call our handler. If it returns true, it means we are delaying the spawn.
            if (DragonEventHandler.INSTANCE.onInitialDragonPreSpawn(fightInstance, this.level)) {
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