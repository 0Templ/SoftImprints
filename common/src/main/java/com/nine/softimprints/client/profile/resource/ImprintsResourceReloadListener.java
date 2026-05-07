package com.nine.softimprints.client.profile.resource;

import com.nine.softimprints.client.profile.io.ProfilesLoader;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class ImprintsResourceReloadListener implements ResourceManagerReloadListener {


    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        ProfilesLoader.reloadProfiles(resourceManager);
    }


}
