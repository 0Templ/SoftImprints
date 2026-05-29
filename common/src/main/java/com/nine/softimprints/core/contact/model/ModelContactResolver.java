package com.nine.softimprints.core.contact.model;

import com.nine.softimprints.core.contact.ContactResolver;
import com.nine.softimprints.core.contact.ContactResult;
import com.nine.softimprints.core.contact.bounds.CompositeContactShape;
import com.nine.softimprints.core.contact.model.area.ModelContactAreaAdapter;
import com.nine.softimprints.core.contact.model.snapshot.ModelContactSnapshot;
import net.minecraft.world.entity.Entity;

public final class ModelContactResolver implements ContactResolver {

    private final ContactResolver fallback;

    public ModelContactResolver(ContactResolver fallback) {
        this.fallback = fallback;
    }

    @Override
    public ContactResult resolve(Entity entity) {
        if (!ModelContactSupport.shouldUseModelContact(entity)) {
            return this.fallback.resolve(entity);
        }

        ModelContactSnapshot snapshot = ModelContactSnapshotCache.resolveUsableSnapshot(entity);
        if (snapshot == null || snapshot.isEmpty()) {
            return resolveFallback(entity);
        }

        CompositeContactShape shape = ModelContactAreaAdapter.adapt(snapshot, entity);
        return shape != null ? new ContactResult(shape, ContactResult.StampStrategy.EXACT) : resolveFallback(entity);
    }

    private ContactResult resolveFallback(Entity entity) {
        if (ModelContactSupport.shouldFallbackToBoundingBox()) {
            return this.fallback.resolve(entity);
        }
        if (!ModelContactSupport.shouldFallbackToLastSnapshot()) {
            return null;
        }

        ModelContactSnapshot snapshot = ModelContactSnapshotCache.resolveLastCompatibleSnapshot(entity);
        if (snapshot == null || snapshot.isEmpty()) {
            return null;
        }
        double yawDeltaRadians = ModelContactSnapshotCache.rotationFromSnapshotToCurrent(snapshot, entity);
        CompositeContactShape shape = ModelContactAreaAdapter.adapt(snapshot, entity, yawDeltaRadians);
        return shape != null ? new ContactResult(shape, ContactResult.StampStrategy.EXACT) : null;
    }
}
