package com.nine.softimprints.client.profile.util;

import com.nine.softimprints.client.core.contact.ContactArea;
import com.nine.softimprints.client.profile.ImprintProfile;
import com.nine.softimprints.client.profile.ImprintProfiles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProfileAreaResolver {

    public static Map<ImprintProfile, ContactArea> resolveProfileAreas(ClientLevel level, ContactArea area) {
        Map<ImprintProfile, List<BlockPos>> grouped = new LinkedHashMap<>();

        double areaLength = area.cellSize * area.size;

        int minX = Mth.floor(area.originX);
        int maxX = Mth.ceil(area.originX + areaLength) - 1;
        int minZ = Mth.floor(area.originZ);
        int maxZ = Mth.ceil(area.originZ + areaLength) - 1;

        int yBase = Mth.floor(area.y);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int dy = 0; dy >= -1; dy--) {
                    BlockPos pos = new BlockPos(x, yBase + dy, z);
                    BlockState state = level.getBlockState(pos);
                    var profile = ImprintProfiles.getProfile(level, pos, state);
                    if (profile == null) continue;
                    if (ProfileSurfaceMatcher.matches(level, pos, profile, area.y)) {
                        grouped.computeIfAbsent(profile, k -> new ArrayList<>()).add(pos);
                        break;
                    }
                }
            }
        }
        Map<ImprintProfile, ContactArea> result = new LinkedHashMap<>();
        for (var entry : grouped.entrySet()) {
            result.put(entry.getKey(), clipAreaToBlocks(area, entry.getValue()));
        }
        return result;
    }



    private static ContactArea clipAreaToBlocks(ContactArea area, List<BlockPos> blocks) {
        boolean[] clipped = new boolean[area.size * area.size];
        for (int gz = 0; gz < area.size; gz++) {
            for (int gx = 0; gx < area.size; gx++) {
                if (!area.bits[gz * area.size + gx]) continue;
                double wx = area.originX + (gx + 0.5) * area.cellSize;
                double wz = area.originZ + (gz + 0.5) * area.cellSize;

                int bx = Mth.floor(wx);
                int bz = Mth.floor(wz);

                for (BlockPos pos : blocks) {
                    if (pos.getX() == bx && pos.getZ() == bz) {
                        clipped[gz * area.size + gx] = true;
                        break;
                    }
                }
            }
        }
        return ContactArea.create(
                area.originX, area.originZ, area.y,
                area.cellSize, area.size, clipped
        );
    }


}
