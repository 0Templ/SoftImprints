package com.nine.softimprints.platform;

import com.google.gson.JsonObject;
import com.nine.softimprints.SICommon;
import com.nine.softimprints.api.meta.update.SIUpdateCandidate;
import com.nine.softimprints.api.meta.update.SIUpdateChannel;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FabricPlatformCoreHelper implements IPlatformCoreHelper {

    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean inDev() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public String modVersion() {
        return FabricLoader.getInstance()
                .getModContainer(SICommon.MODID)
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElse("0.0.0");
    }


    private static final String UPDATE_JSON_URL =
            "https://raw.githubusercontent.com/0Templ/ModVersions/refs/heads/main/fabric/snow-imprints.json";


    @Override
    public String loader() {
        return "fabric";
    }

    @Override
    public boolean modLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }

}
