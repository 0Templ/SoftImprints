package com.nine.softimprints.client.core.contact.util;

import com.nine.softimprints.client.core.contact.bounds.CompositeContactShape;
import com.nine.softimprints.client.core.contact.bounds.ContactBounds;
import com.nine.softimprints.client.profile.ImprintProfile;
import com.nine.softimprints.client.profile.ImprintProfiles;
import com.nine.softimprints.client.profile.util.ProfileSurfaceMatcher;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProfileShapesResolver {

    public static List<ProfileContactArea> resolveProfileAreas(
            ClientLevel level,
            CompositeContactShape shape
    ) {
        Map<ImprintProfile, List<ContactBounds>> grouped = new LinkedHashMap<>();

        ContactBounds bounds = shape.bounds();

        int minX = Mth.floor(bounds.minX());
        int maxX = Mth.ceil(bounds.maxX()) - 1;
        int minZ = Mth.floor(bounds.minZ());
        int maxZ = Mth.ceil(bounds.maxZ()) - 1;
        int yBase = Mth.floor(shape.y());

        var pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int dy = 0; dy >= -1; dy--) {
                    pos.set(x, yBase + dy, z);

                    BlockState state = level.getBlockState(pos);
                    ImprintProfile profile = ImprintProfiles.getProfile(level, pos, state);
                    if (profile == null) continue;

                    if (!ProfileSurfaceMatcher.matches(level, pos, profile, shape.y())) {
                        continue;
                    }

                    addClippedParts(grouped, profile, shape, x, z);
                    break;
                }
            }
        }

        List<ProfileContactArea> result = new ArrayList<>(grouped.size());
        for (var entry : grouped.entrySet()) {
            var builder = new CompositeContactShape.Builder();
            entry.getValue().forEach(builder::add);
            if (builder.isEmpty()) continue;
            result.add(new ProfileContactArea(entry.getKey(), builder.build(shape.y())));
        }
        return result;
    }

    private static void addClippedParts(
            Map<ImprintProfile, List<ContactBounds>> grouped,
            ImprintProfile profile,
            CompositeContactShape shape,
            int blockX,
            int blockZ
    ) {
        double maxX = blockX + 1.0D;
        double maxZ = blockZ + 1.0D;

        for (ContactBounds part : shape.parts()) {
            ContactBounds clipped = part.intersection(blockX, blockZ, maxX, maxZ);
            if (clipped != null) {
                grouped.computeIfAbsent(profile, k -> new ArrayList<>()).add(clipped);
            }
        }
    }
}
