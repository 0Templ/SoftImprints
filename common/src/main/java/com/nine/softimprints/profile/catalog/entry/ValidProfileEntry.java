package com.nine.softimprints.profile.catalog.entry;

import com.nine.softimprints.profile.ImprintProfile;
import net.minecraft.resources.Identifier;

public record ValidProfileEntry(
        Identifier id,
        ImprintProfile profile
) implements ImprintProfileEntry {



}