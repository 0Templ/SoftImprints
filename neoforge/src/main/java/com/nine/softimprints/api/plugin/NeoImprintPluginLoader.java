package com.nine.softimprints.api.plugin;

import com.nine.softimprints.SICommon;
import com.nine.softimprints.client.api.plugin.ImprintPlugin;
import com.nine.softimprints.client.api.plugin.ImprintPluginLoader;
import net.neoforged.fml.ModList;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NeoImprintPluginLoader {

    public static void load(){
        List<ImprintPlugin> plugins = new ArrayList<>();
        for (String className : findPluginClasses()) {
            try {
                Class<?> raw = Class.forName(className);
                Class<? extends ImprintPlugin> pluginClass = raw.asSubclass(ImprintPlugin.class);

                ImprintPlugin plugin = pluginClass.getDeclaredConstructor().newInstance();
                plugins.add(plugin);

            } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
                SICommon.LOGGER.warn("Failed to load Soft Imprints plugin {}", className, e);
            }
        }

        ImprintPluginLoader.load(plugins);
    }

    private static Set<String> findPluginClasses() {
        Set<String> result = new LinkedHashSet<>();

        for (var scanData : ModList.get().getAllScanData()) {
            scanData.getAnnotatedBy(SoftImprintsPlugin.class, ElementType.TYPE)
                    .map(annotation -> annotation.clazz().getClassName())
                    .forEach(result::add);
        }

        return result;
    }


}
