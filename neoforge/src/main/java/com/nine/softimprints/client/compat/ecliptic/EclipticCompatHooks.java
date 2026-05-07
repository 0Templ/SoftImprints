package com.nine.softimprints.client.compat.ecliptic;

import com.teamtea.eclipticseasons.api.EclipticSeasonsApi;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class EclipticCompatHooks {

    private EclipticCompatHooks() {
    }

    public static boolean isSnowySurface(Level level, BlockPos pos, BlockState state) {
        return EclipticSeasonsApi.getInstance().isSnowyBlock(level, state, pos);
    }
}
