package com.nine.softimprints;

import com.nine.softimprints.client.api.plugin.ImprintPlugin;
import com.nine.softimprints.client.api.plugin.ImprintPluginLoader;
import com.nine.softimprints.client.profile.resource.ImprintsResourceReloadListener;
import com.nine.softimprints.event.FabricClientEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public class SIFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SICommon.init();

        loadPlugins();
        FabricClientEvents.init();

        registerResourceListener();
        registerBuiltinPacks();
    }

    private void loadPlugins() {
        ImprintPluginLoader.load(FabricLoader.getInstance()
                .getEntrypoints("softimprints", ImprintPlugin.class));
    }

    private void registerResourceListener(){
        var loader = ResourceLoader.get(PackType.CLIENT_RESOURCES);
        var id = Identifier.fromNamespaceAndPath(SICommon.MODID, "imprint_profiles");

        loader.registerReloadListener(id, new ImprintsResourceReloadListener());

        // Tests
        loader.addListenerOrdering(id, ResourceReloaderKeys.Client.MODELS);
        loader.addListenerOrdering(id, ResourceReloaderKeys.BEFORE_VANILLA);
    }

    private void registerBuiltinPacks() {
        var container = FabricLoader.getInstance().getModContainer(SICommon.MODID).orElseThrow();
        ResourceLoader.registerBuiltinPack(
                Identifier.fromNamespaceAndPath(SICommon.MODID, "debug"),
                container,
                Component.translatable("pack.softimprints.debug"),
                PackActivationType.NORMAL
        );
    }

}
