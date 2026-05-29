package com.nine.softimprints.profile.catalog.entry;

import net.minecraft.resources.Identifier;

public sealed interface ImprintProfileEntry
        permits ValidProfileEntry, InvalidProfileEntry {

    Identifier id();


}