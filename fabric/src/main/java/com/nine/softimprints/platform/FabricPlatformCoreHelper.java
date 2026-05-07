package com.nine.softimprints.platform;

import com.nine.softimprints.client.platform.IPlatformCoreHelper;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class FabricPlatformCoreHelper implements IPlatformCoreHelper {

    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

}
