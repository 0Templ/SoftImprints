package com.nine.softimprints.profile.catalog.entry.issue;


import net.minecraft.network.chat.Component;

import java.util.List;

public sealed interface ProfileIssue permits UnsupportedSchemaIssue {



    Component title();

    Component tooltip();

    List<Component> details();



}