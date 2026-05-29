package com.nine.softimprints.api.plugin;

import net.minecraft.resources.Identifier;

public interface ImprintPlugin {

    void register(ImprintRegistrar registrar);

    ImprintPluginInfo info();

    Identifier id();


}
