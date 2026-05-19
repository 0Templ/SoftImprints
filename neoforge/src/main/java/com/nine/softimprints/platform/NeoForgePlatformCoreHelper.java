package com.nine.softimprints.platform;

import com.nine.softimprints.client.platform.IPlatformCoreHelper;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class NeoForgePlatformCoreHelper implements IPlatformCoreHelper {

    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean inDev() {
        return !FMLLoader.getCurrent().isProduction();
    }

}
