package com.nine.softimprints.client.core.contact.model;

import com.nine.softimprints.client.core.contact.ContactResolver;
import com.nine.softimprints.client.core.contact.ContactResult;
import com.nine.softimprints.client.core.contact.model.area.ModelContactAreaAdapter;
import com.nine.softimprints.client.core.contact.model.snapshot.ModelContactSnapshot;
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

        ContactResult result = ModelContactAreaAdapter.adapt(snapshot, entity);
        return result != null ? result : resolveFallback(entity);
    }

    private ContactResult resolveFallback(Entity entity) {
        return ModelContactSupport.shouldFallbackToBoundingBox()
                ? this.fallback.resolve(entity)
                : null;
    }
}
