package com.nine.softimprints.client.core;

import com.nine.softimprints.client.core.contact.ContactArea;
import com.nine.softimprints.client.core.contact.ContactResolvers;
import com.nine.softimprints.client.core.placement.BlockMask;
import com.nine.softimprints.client.core.placement.StampColumnHelper;
import com.nine.softimprints.client.core.stamp.StampGenerator;
import com.nine.softimprints.client.core.stamp.StampMask;
import com.nine.softimprints.client.core.stamp.StampPropertiesFactory;
import com.nine.softimprints.client.profile.ImprintProfile;
import com.nine.softimprints.client.profile.util.ProfileAreaResolver;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.ArrayList;
import java.util.List;

public class ImprintProcessor {

    public record Result(List<BlockMask> masks, IntSet applied) {
    }

    public static Result getBlockMasks(ClientLevel level, IntSet ids) {

        List<BlockMask> ret = new ArrayList<>();
        IntSet applied = new IntOpenHashSet();
        List<ContactArea> rawAreas = new ArrayList<>();
        List<ContactArea> clippedAreas = new ArrayList<>();

        for (int id : ids) {
            var entity = level.getEntity(id);
            if (entity == null) {
                applied.add(id);
                continue;
            }

            var contactResolver = ContactResolvers.getResolver(entity);
            var result = contactResolver.resolve(entity);
            if (result == null) {
                continue;
            }
            applied.add(id);
            rawAreas.add(result.area());
            var resolvedAreas = ProfileAreaResolver.resolveProfileAreas(level, result.area());
            for (var data : resolvedAreas.entrySet()) {
                ImprintProfile profile = data.getKey();
                ContactArea clippedArea = data.getValue();
                clippedAreas.add(clippedArea);

                // TODO: implement dependence between fallDistance/deltaMovenet.y() and stamp props
                var stampProps = StampPropertiesFactory.create(entity);
                var seed = StampPropertiesFactory.createSeed(entity, clippedArea, id);

                StampMask mask = StampGenerator.generate(
                        profile,
                        clippedArea.bits,
                        clippedArea.size,
                        seed, result.strategy(),
                        stampProps
                );

                var columnSlices = StampColumnHelper.slice(clippedArea, mask);

                ret.addAll(StampColumnHelper.columnsToBlocks(level, columnSlices, profile, clippedArea.y));
            }
        }

        return new Result(ret, applied);

    }

}
