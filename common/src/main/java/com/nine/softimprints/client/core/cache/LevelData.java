package com.nine.softimprints.client.core.cache;

import net.minecraft.core.BlockPos;

public class LevelData {

    private final ImprintCache maps = new ImprintCache();
    private final BlockRenderCache models = new BlockRenderCache();

    public ImprintCache getImprintCache(){
        return maps;
    }

    public BlockRenderCache getBlockRenderCache(){
        return models;
    }

    public void clearAt(long pos) {
        maps.clearAt(pos);
        models.clearAt(pos);
    }

    public void clearAtChunk(int chunkX, int chunkZ) {
        int minBlockX = chunkX << 4;
        int maxBlockX = minBlockX + 15;
        int minBlockZ = chunkZ << 4;
        int maxBlockZ = minBlockZ + 15;

        maps.clearIf(packed -> {
            int x = BlockPos.getX(packed);
            int z = BlockPos.getZ(packed);
            return x >= minBlockX && x <= maxBlockX && z >= minBlockZ && z <= maxBlockZ;
        });
        models.clearIf(packed -> {
            int x = BlockPos.getX(packed);
            int z = BlockPos.getZ(packed);
            return x >= minBlockX && x <= maxBlockX && z >= minBlockZ && z <= maxBlockZ;
        });
    }

}
