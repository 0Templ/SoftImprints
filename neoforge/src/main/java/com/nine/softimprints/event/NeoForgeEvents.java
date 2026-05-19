package com.nine.softimprints.event;

import com.nine.softimprints.SICommon;
import com.nine.softimprints.api.plugin.NeoImprintPluginLoader;
import com.nine.softimprints.client.SILifecycle;
import com.nine.softimprints.client.profile.ImprintProfiles;
import com.nine.softimprints.client.profile.resolver.ProfileResolverEntry;
import com.nine.softimprints.client.profile.io.ProfilesLoader;
import com.nine.softimprints.client.profile.resource.ImprintsResourceReloadListener;
import com.nine.softimprints.model.NeoBaseNeoImprintableStateModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

public final class NeoForgeEvents {

    private NeoForgeEvents() {
    }

    @EventBusSubscriber(modid = SICommon.MODID, value = Dist.CLIENT)
    public static final class ClientEvents {

        @SubscribeEvent
        public static void addPackFinders(AddPackFindersEvent event) {
            event.addPackFinders(
                    Identifier.fromNamespaceAndPath(SICommon.MODID, "resourcepacks/debug"),
                    PackType.CLIENT_RESOURCES,
                    Component.translatable("imprint_pack.softimprints.debug"),
                    PackSource.BUILT_IN,
                    false,
                    Pack.Position.TOP
            );
        }

        @SubscribeEvent
        public static void onClientSetup(FMLCommonSetupEvent event) {
            NeoImprintPluginLoader.load();
        }

        @SubscribeEvent
        public static void onAddClientReloadListeners(AddClientReloadListenersEvent event) {
            event.addListener(Identifier.fromNamespaceAndPath(SICommon.MODID, "imprint_profiles"),
                    new ImprintsResourceReloadListener());
        }


        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onModifyBakingResultProfile(ModelEvent.ModifyBakingResult event) {
            ProfilesLoader.ensureLoaded(Minecraft.getInstance().getResourceManager());
            for (Block block : ImprintProfiles.supportedBlocks()) {
                wrapBlock(event, block);
            }
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            SILifecycle.onClientTick(Minecraft.getInstance());
        }

        @SubscribeEvent
        public static void onClientStarted(ClientStartedEvent event) {
            SILifecycle.onClientStarted(event.getClient());
        }

        @SubscribeEvent
        public static void onClientStopping(ClientStoppingEvent event) {
            SILifecycle.onDisconnect(event.getClient());
        }

        @SubscribeEvent
        public static void onClientLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
            SILifecycle.onJoin(Minecraft.getInstance());
        }

        @SubscribeEvent
        public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            SILifecycle.onDisconnect(Minecraft.getInstance());
        }

        @SubscribeEvent
        public static void onLevelLoad(LevelEvent.Load evt) {
            if (evt.getLevel() instanceof ClientLevel cl) {
                SILifecycle.syncLevelCache(cl);
            }
        }

        @SubscribeEvent
        public static void onLevelUnload(LevelEvent.Unload evt) {
            if (evt.getLevel() instanceof ClientLevel) {
                SILifecycle.onDisconnect(Minecraft.getInstance());
            }
        }

        @SubscribeEvent
        public static void onChunkUnload(ChunkEvent.Unload event) {
            if (event.getLevel() instanceof ClientLevel) {
                SILifecycle.onChunkUnload(event.getChunk());
            }
        }
    }

    private static void wrapBlock(ModelEvent.ModifyBakingResult event, Block block) {
        var models = event.getBakingResult().blockStateModels();
        for (var state : block.getStateDefinition().getPossibleStates()) {
            models.computeIfPresent(state, (ignored, original) -> wrap(original));
        }
    }

    private static BlockStateModel wrap(BlockStateModel original) {
        return wrap(original, null);
    }

    private static BlockStateModel wrap(BlockStateModel original, ProfileResolverEntry resolver) {
        if (original instanceof NeoBaseNeoImprintableStateModel) {
            return resolver == null
                    ? original
                    : ((NeoBaseNeoImprintableStateModel) original).withModelResolver(resolver);
        }
        NeoBaseNeoImprintableStateModel wrapped = new NeoBaseNeoImprintableStateModel(original);
        return resolver == null ? wrapped : wrapped.withModelResolver(resolver);
    }

}
