package com.nine.softimprints.client.core;

import com.nine.softimprints.client.core.contact.bounds.CompositeContactShape;
import com.nine.softimprints.client.core.contact.ContactResolvers;
import com.nine.softimprints.client.core.contact.raster.ContactRaster;
import com.nine.softimprints.client.core.contact.raster.ContactRasterizer;
import com.nine.softimprints.client.core.contact.raster.StampRaster;
import com.nine.softimprints.client.core.placement.BlockMask;
import com.nine.softimprints.client.core.placement.StampColumnHelper;
import com.nine.softimprints.client.core.stamp.StampGenerator;
import com.nine.softimprints.client.core.stamp.StampMask;
import com.nine.softimprints.client.core.stamp.StampPropertiesFactory;
import com.nine.softimprints.client.profile.ImprintProfile;
import com.nine.softimprints.client.core.contact.util.ProfileShapesResolver;
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
            var resolvedShape = ProfileShapesResolver.resolveProfileAreas(level, result.shape());

            for (var profileShape : resolvedShape) {
                ImprintProfile profile = profileShape.profile();
                CompositeContactShape shape = profileShape.shape();

                ContactRaster contact = ContactRasterizer.rasterize(
                        shape,
                        profile.resolution().mapSize()
                );
                if (contact == null) continue;

                var stampProps = StampPropertiesFactory.create(entity);
                var seed = StampPropertiesFactory.createSeed(entity, contact, shape.y(), id);
                StampRaster stamp = StampGenerator.generate(
                        profile,
                        contact,
                        seed,
                        result.strategy(),
                        stampProps
                );


                var columnSlices = StampColumnHelper.slice(profile, stamp);
                var blockMasks = StampColumnHelper.columnsToBlocks(level, columnSlices, profile, shape.y());
                ret.addAll(blockMasks);
            }
        }

        return new Result(ret, applied);

    }

}
