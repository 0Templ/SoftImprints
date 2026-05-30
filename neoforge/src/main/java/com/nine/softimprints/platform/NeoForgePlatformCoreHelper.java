package com.nine.softimprints.platform;

import com.nine.softimprints.SICommon;
import com.nine.softimprints.platform.IPlatformCoreHelper;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class NeoForgePlatformCoreHelper implements IPlatformCoreHelper {

    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean inDevEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public String modVersion() {
        return ModList.get()
                .getModContainerById(SICommon.MODID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("0.0.0");
    }


    @Override
    public String currentLoader() {
        return "neoforge";
    }

    @Override
    public boolean isModLoaded(String id) {
        return ModList.get().isLoaded(id);
    }

}
