package com.nine.softimprints.event;

import com.nine.softimprints.client.SILifecycle;
import com.nine.softimprints.client.profile.ImprintProfiles;
import com.nine.softimprints.client.profile.ProfileResolverEntry;
import com.nine.softimprints.client.profile.io.ProfilesLoader;
import com.nine.softimprints.model.BasicImprintableStateModel;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

public class FabricClientEvents {

    public static void init() {
        registerLifecycleEvents();
        registerModelWrapEvent();
    }

    private static void registerLifecycleEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(SILifecycle::onClientTick);

        ClientLifecycleEvents.CLIENT_STARTED.register(SILifecycle::onClientStarted);

        ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> SILifecycle.onJoin(client));
        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> SILifecycle.onDisconnect(client));


        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> SILifecycle.onChunkUnload(chunk));

    }


    private static void registerModelWrapEvent() {
        ModelLoadingPlugin.register(context -> context.modifyBlockModelAfterBake()
                .register(ModelModifier.WRAP_PHASE, FabricClientEvents::wrapModels));
    }

    private static BlockStateModel wrapModels(BlockStateModel model, ModelModifier.AfterBakeBlock.Context ctx) {
        ProfilesLoader.ensureLoaded(Minecraft.getInstance().getResourceManager());
        var blockMatch = ImprintProfiles.supportsBlock(ctx.state().getBlock());
        return blockMatch ? wrap(model) : model;
    }

    private static BlockStateModel wrap(BlockStateModel original) {
        return wrap(original, null);
    }

    private static BlockStateModel wrap(BlockStateModel original, ProfileResolverEntry resolver) {
        if (original instanceof BasicImprintableStateModel imprintable) {
            return resolver == null ? original : imprintable.withModelResolver(resolver);
        }
        BasicImprintableStateModel wrapped = new BasicImprintableStateModel(original);
        return resolver == null ? wrapped : wrapped.withModelResolver(resolver);
    }

    public static BlockStateModel wrappedModel(BlockStateModel model) {
        return model instanceof BasicImprintableStateModel imprintable
                ? imprintable.wrappedModel()
                : model;
    }

}
