package com.nine.softimprints.ui.context;

import com.nine.softimprints.profile.ImprintProfile;
import com.nine.softimprints.profile.catalog.entry.ImprintProfileEntry;
import com.nine.softimprints.profile.io.ProfilesSaver;
import com.nine.softimprints.ui.draft.DraftHolder;
import com.nine.softimprints.ui.draft.ImprintProfileDraft;
import net.minecraft.resources.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProfilesSession {

    private final LinkedHashMap<Identifier, ImprintProfileEntry> entries;
    private final LinkedHashMap<Identifier, DraftHolder<ImprintProfile>> profiles;

    public ProfilesSession(
            Collection<ImprintProfileEntry> entries,
            Map<Identifier, ImprintProfile> builtins,
            Map<Identifier, ImprintProfile> merged
    ) {
        this.entries = new LinkedHashMap<>();
        this.profiles = new LinkedHashMap<>();

        for (var entry : entries) {
            this.entries.put(entry.id(), entry);

            ImprintProfile current = merged.get(entry.id());
            if (current == null) continue;

            ImprintProfile builtin = builtins.getOrDefault(entry.id(), current);
            this.profiles.put(entry.id(), new ImprintProfileDraft(current, builtin));
        }
    }

    public List<DraftHolder<ImprintProfile>> drafts() {
        return List.copyOf(profiles.values());
    }

    public List<Identifier> ids() {
        return List.copyOf(entries.keySet());
    }

    @Nullable
    public DraftHolder<ImprintProfile> draft(Identifier id) {
        return profiles.getOrDefault(id, null);
    }

    @Nonnull
    public ImprintProfileEntry entry(Identifier id) {
        var ret = entries.get(id);
        if (ret == null) {
            throw new IllegalStateException("Profile entry is not initialized: " + id);
        }
        return ret;
    }

    @Nullable
    public ImprintProfileEntry entryOrNull(Identifier id) {
        return entries.get(id);
    }

    @Nonnull
    public ImprintProfileEntry entryOrFirst(@Nullable Identifier id) {
        if (id != null) {
            var fromId = entries.get(id);
            if (fromId != null) return fromId;
        }
        return entries.values().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No profile entries loaded"));
    }

    public void saveChanged() {
        profiles.forEach((id, draft) -> {
            if (draft.hasUnsavedChanges()) {
                ProfilesSaver.save(id, draft.getDraft());
                draft.applyDraft();
            }
        });
    }
}
