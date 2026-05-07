package com.nine.softimprints.client.api.plugin;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

public record ImprintPluginInfo(
        Identifier id,
        Function<Boolean, Component> title,
        Component tooltip,
        boolean enabledByDefault
) {}