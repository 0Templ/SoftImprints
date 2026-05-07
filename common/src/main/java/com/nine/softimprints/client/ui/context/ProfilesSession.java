package com.nine.softimprints.client.ui.context;

import com.nine.softimprints.client.profile.ImprintProfile;
import com.nine.softimprints.client.profile.io.ProfilesSaver;
import com.nine.softimprints.client.profile.util.ImprintProfilePair;
import com.nine.softimprints.client.ui.draft.DraftHolder;
import com.nine.softimprints.client.ui.draft.ImprintProfileDraft;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ProfilesSession {

    private final LinkedHashMap<Identifier, DraftHolder<ImprintProfile>> profiles;

    public ProfilesSession(Collection<ImprintProfilePair> source) {
        this.profiles = source.stream()
                .collect(Collectors.toMap(
                        pair -> pair.profile().id,
                        (pair) -> new ImprintProfileDraft(pair.profile(), pair.builtin()),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    public List<DraftHolder<ImprintProfile>> drafts() {
        return List.copyOf(profiles.values());
    }

    public List<Identifier> ids() {
        return List.copyOf(profiles.keySet());
    }

    public Identifier nextId(Identifier from) {
        int index = ids().indexOf(from);
        int next = index + 1;
        if (next >= ids().size()) return ids().getFirst();
        return ids().get(next);
    }


    public Identifier previousId(Identifier from) {
        int index = ids().indexOf(from);
        int next = index - 1;
        if (next < 0) return ids().getLast();
        return ids().get(next);
    }

    @Nullable
    public DraftHolder<ImprintProfile> firstDraft() {
        return profiles.values().stream().findFirst().orElse(null);
    }

    @Nullable
    public DraftHolder<ImprintProfile> draft(Identifier id) {
        return profiles.getOrDefault(id, null);
    }

    public DraftHolder<ImprintProfile> draftOrFirst(Identifier id) {
        return profiles.getOrDefault(id, firstDraft());
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
