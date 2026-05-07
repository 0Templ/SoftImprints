package com.nine.softimprints.client.profile.util;

import com.nine.softimprints.client.core.Constants;
import com.nine.softimprints.client.profile.ImprintProfile;
import com.nine.softimprints.client.profile.ImprintProfiles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;

public class ProfileSurfaceMatcher {

    public static boolean matches(
            ClientLevel level,
            BlockPos pos,
            ImprintProfile profile,
            double contactY
    ) {
        BlockState state = level.getBlockState(pos);
        var resolved = ImprintProfiles.getProfile(level, pos, state);
        if (resolved == null || !Objects.equals(resolved.id(), profile.id())) {
            return false;
        }

        double blockTop = pos.getY() + getTopHeight(state, level, pos);
        return Math.abs(contactY - blockTop) < Constants.BASE_ALLOWED_HEIGHT_CHECK;
    }

    public static double getTopHeight(BlockState state, ClientLevel level, BlockPos pos) {
        VoxelShape shape = state.getCollisionShape(level, pos);
        if (shape.isEmpty()) {
            shape = state.getShape(level, pos);
        }
        return shape.isEmpty() ? 0.0 : shape.max(Direction.Axis.Y);
    }


}
