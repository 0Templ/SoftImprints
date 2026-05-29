package com.nine.softimprints.platform;

import com.google.gson.JsonObject;
import com.nine.softimprints.SICommon;
import com.nine.softimprints.api.meta.update.SIUpdateCandidate;
import com.nine.softimprints.api.meta.update.SIUpdateChannel;
import net.minecraft.SharedConstants;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NeoForgePlatformCoreHelper implements IPlatformCoreHelper {

    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean inDev() {
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
    public String loader() {
        return "neoforge";
    }

    @Override
    public boolean modLoaded(String id) {
        return ModList.get().isLoaded(id);
    }

}
