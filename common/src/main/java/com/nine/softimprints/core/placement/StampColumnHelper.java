package com.nine.softimprints.core.placement;

import com.nine.softimprints.core.contact.raster.StampRaster;
import com.nine.softimprints.profile.ImprintProfile;
import com.nine.softimprints.profile.ImprintProfiles;
import com.nine.softimprints.profile.util.ProfileSurfaceMatcher;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StampColumnHelper {

    public static List<ColumnMask> slice(
            ImprintProfile profile,
            StampRaster raster
    ) {
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

    public static List<BlockMask> columnsToBlocks(
            ClientLevel level,
            List<ColumnMask> columns,
            ImprintProfile profile,
            double y
    ) {
        List<BlockMask> ret = new ArrayList<>();
        int yBase = Mth.floor(y);

        for (var column : columns) {

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

