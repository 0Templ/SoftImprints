package com.nine.softimprints.client.ui.component.profile;

import com.nine.softimprints.client.profile.ImprintProfile;
import net.minecraft.resources.Identifier;

import java.util.List;

public record ProfilesGroup(
        String namespace,
        List<Identifier> profiles
) {

    public ProfilesGroup {
        profiles = List.copyOf(profiles);
    }

    public int size() {
        return profiles.size();
    }

    public Identifier atLoop(int index) {
        return profiles.get(Math.floorMod(index, profiles.size()));
    }

}
