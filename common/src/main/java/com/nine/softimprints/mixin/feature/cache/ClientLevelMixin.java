package com.nine.softimprints.mixin.feature.cache;

import com.nine.softimprints.client.core.cache.CacheAccess;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(method = "setBlocksDirty", at = @At("HEAD"))
    private void softimprints$clearImprintsOnBlockChanged(
            BlockPos pos,
            BlockState oldState,
            BlockState newState,
            CallbackInfo ci
    ) {
        if (oldState == newState) {
            return;
        }
        var posLong = pos.asLong();
        var cache = CacheAccess.current();
        if (cache != null) {
            cache.clearAt(posLong);
        }
    }

}
