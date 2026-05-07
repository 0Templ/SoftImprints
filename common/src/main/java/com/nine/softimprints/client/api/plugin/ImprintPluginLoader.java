package com.nine.softimprints.client.api.plugin;

import java.util.Collection;

public class ImprintPluginLoader {

    public static void load(Collection<ImprintPlugin> plugins) {
        ImprintRegistrar registrar = ImprintPlugins.registrar();

        plugins.forEach(plugin -> {
            plugin.register(registrar);
            ImprintPlugins.addMeta(plugin.info());
        });
    }

}
