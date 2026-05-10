package com.nine.softimprints.client.core.placement;

import com.nine.softimprints.client.core.Constants;
import com.nine.softimprints.client.core.contact.ContactArea;
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

    private static final int BLOCK_MAP_SIZE = 16;
    private static final int BLOCK_MAP_AREA = BLOCK_MAP_SIZE * BLOCK_MAP_SIZE;

    // StampMask must be generated from this ContactArea
    public static List<ColumnMask> slice(ContactArea area, StampMask stampMask) {
        int stampSize = stampMask.size();
        byte[] stamp = stampMask.mask();

        int totalPixels = (stampMask.size() * Constants.BASIC_RESOLUTION);
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
                byte[] blockMap = new byte[BLOCK_MAP_AREA];
                boolean hasData = false;

                for (int py = 0; py < BLOCK_MAP_SIZE; py++) {
                    for (int px = 0; px < BLOCK_MAP_SIZE; px++) {
                        double worldX = blockX + (px + 0.5D) / BLOCK_MAP_SIZE;
                        double worldZ = blockZ + (py + 0.5D) / BLOCK_MAP_SIZE;

                        int gx = Mth.floor((worldX - originX) / cellSize);
                        int gz = Mth.floor((worldZ - originZ) / cellSize);

                        if (gx < 0 || gx >= stampSize || gz < 0 || gz >= stampSize) {
                            continue;
                        }

                        byte value = stamp[gz * stampSize + gx];
                        if (value == 0) {
                            continue;
                        }

                        blockMap[py * BLOCK_MAP_SIZE + px] = value;
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
                    ret.add(new BlockMask(pos.asLong(), column.map()));
                    break;
                }
            }
        }

        return ret;
    }
}

