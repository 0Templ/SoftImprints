package com.nine.softimprints.profile.catalog.entry;

import com.nine.softimprints.profile.catalog.entry.issue.ProfileIssue;
import com.nine.softimprints.profile.io.json.JsonSource;
import net.minecraft.resources.Identifier;

public record InvalidProfileEntry(
        Identifier id,
        ProfileIssue issue,
        JsonSource source
) implements ImprintProfileEntry {

}
