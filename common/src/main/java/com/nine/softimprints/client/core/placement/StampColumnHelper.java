package com.nine.softimprints.client.core.placement;

import com.nine.softimprints.client.core.contact.legacy.ContactArea;
import com.nine.softimprints.client.core.contact.raster.StampRaster;
import com.nine.softimprints.client.core.stamp.StampMask;
import com.nine.softimprints.client.profile.ImprintProfile;
import com.nine.softimprints.client.profile.ImprintProfiles;
import com.nine.softimprints.client.profile.util.ProfileSurfaceMatcher;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StampColumnHelper {

    public static List<ColumnMask> slice(ImprintProfile profile, StampRaster raster) {
        int width = raster.width();
        int height = raster.height();

        byte[] mask = raster.mask();
        double cellSize = raster.cellSize();

        int columnRes = profile.resolution().mapSize();

        double originX = raster.originX();
        double originZ = raster.originZ();

        int minX = Mth.floor(raster.originX());
        int maxX = Mth.ceil(raster.originX() + cellSize * width) - 1;
        int minZ = Mth.floor(raster.originZ());
        int maxZ = Mth.ceil(raster.originZ() + cellSize * height) - 1;

        List<ColumnMask> ret = new ArrayList<>();

        for (int blockX = minX; blockX <= maxX; blockX++) {
            for (int blockZ = minZ; blockZ <= maxZ; blockZ++) {
                byte[] map = new byte[columnRes * columnRes];

                boolean hasData = false;

                for (int pz = 0; pz < columnRes; pz++) {
                    for (int px = 0; px < columnRes; px++) {
                        double wX = blockX + (px + 0.5D) / columnRes;
                        double wZ = blockZ + (pz + 0.5D) / columnRes;

                        int gx = Mth.floor((wX - originX) / cellSize);
                        int gz = Mth.floor((wZ - originZ) / cellSize);

                        if (gx < 0 || gx >= width || gz < 0 || gz >= height) {
                            continue;
                        }

                        byte value = mask[gz * width + gx];
                        if (value == 0) {
                            continue;
                        }

                        map[pz * columnRes + px] = value;
                        hasData = true;

                    }
                }
                if (!hasData) continue;
                ret.add(new ColumnMask(blockX, blockZ, map));
            }
        }

        return ret;
    }


    public static List<ColumnMask> slice(ImprintProfile profile, ContactArea area, StampMask stampMask) {
        int stampSize = stampMask.size();
        byte[] stamp = stampMask.mask();
        
        int mapSize = profile.resolution().mapSize();

        int totalPixels = (stampMask.size() * mapSize);
        double cellSize = (double) stampMask.size() / totalPixels;

        double originX = area.originX - stampMask.padding() * cellSize;
        double originZ = area.originZ - stampMask.padding() * cellSize;
        double worldLength = stampSize * cellSize;

        int minX = Mth.floor(originX);
        int maxX = Mth.ceil(originX + worldLength) - 1;
        int minZ = Mth.floor(originZ);
        int maxZ = Mth.ceil(originZ + worldLength) - 1;

        List<ColumnMask> result = new ArrayList<>();
        for (int blockX = minX; blockX <= maxX; blockX++) {
            for (int blockZ = minZ; blockZ <= maxZ; blockZ++) {
                byte[] blockMap = new byte[mapSize * mapSize];
                boolean hasData = false;

                for (int py = 0; py < mapSize; py++) {
                    for (int px = 0; px < mapSize; px++) {
                        double worldX = blockX + (px + 0.5D) / mapSize;
                        double worldZ = blockZ + (py + 0.5D) / mapSize;

                        int gx = Mth.floor((worldX - originX) / cellSize);
                        int gz = Mth.floor((worldZ - originZ) / cellSize);

                        if (gx < 0 || gx >= stampSize || gz < 0 || gz >= stampSize) {
                            continue;
                        }

                        byte value = stamp[gz * stampSize + gx];
                        if (value == 0) {
                            continue;
                        }

                        blockMap[py * mapSize + px] = value;
                        hasData = true;
                    }
                }

                if (hasData) {
                    result.add(new ColumnMask(blockX, blockZ, blockMap));
                }
            }
        }

        return result;
    }

    public static List<BlockMask> columnsToBlocks(
            ClientLevel level,
            List<ColumnMask> columns,
            ImprintProfile profile,
            double y
    ) {
        List<BlockMask> ret = new ArrayList<>();
        int yBase = Mth.floor(y);

        for (var column : columns){

            for (int dy = 0; dy >= -1; dy--) {
                BlockPos pos = new BlockPos(column.x(), yBase + dy, column.z());
                BlockState state = level.getBlockState(pos);

                var thisProfile = ImprintProfiles.getProfile(level, pos, state);
                if (thisProfile == null || !Objects.equals(thisProfile.id(), profile.id())) continue;

                if (ProfileSurfaceMatcher.matches(level, pos, profile, y)) {
                    ret.add(new BlockMask(pos.asLong(), profile.resolution().mapSize(), column.map()));
                    break;
                }
            }
        }

        return ret;
    }
}

