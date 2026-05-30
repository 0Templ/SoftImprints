package com.nine.softimprints.compat.ecliptic;

import com.nine.softimprints.SICommon;
import com.nine.softimprints.api.plugin.ImprintPlugin;
import com.nine.softimprints.api.plugin.ImprintPluginInfo;
import com.nine.softimprints.api.plugin.ImprintRegistrar;
import com.nine.softimprints.api.plugin.SoftImprintsPlugin;
import com.nine.softimprints.platform.Platform;
import com.nine.softimprints.profile.resolver.ProfileResolverEntry;
import com.nine.softimprints.ui.util.constant.SIText;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

@SoftImprintsPlugin
public class EclipticImprintPlugin implements ImprintPlugin {

    private static final Identifier ID = Identifier.fromNamespaceAndPath(SICommon.MODID, "ecliptic_seasons");

    private static final String ECLIPTIC_MOD_ID = "eclipticseasons";

    @Override
    public void register(ImprintRegistrar registrar) {
        if (!Platform.CORE.isModLoaded(ECLIPTIC_MOD_ID)) {
            return;
        }
        if (!EclipticCompat.isPresent()) {
            return;
        }
        registrar.registerResolver(
                fullBlocks(),
                new ProfileResolverEntry(ID, 100, EclipticCompat.snowySurfaceResolver())
        );
    }

    @Override
    public ImprintPluginInfo info() {
        return new ImprintPluginInfo(
                Identifier.fromNamespaceAndPath(SICommon.MODID, "ecliptic_seasons"),
                (enabled ->
                        Component.translatable("plugin.softimprints.ecliptic_seasons", SIText.onOffState(enabled))),
                Component.translatable("plugin.softimprints.ecliptic_seasons.tooltip"),
                true
        );
    }


    private static List<Block> fullBlocks() {
        List<Block> ret = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (hasFullBlockState(block)) {
                ret.add(block);
            }
        }
        return ret;
    }

    private static boolean hasFullBlockState(Block block) {
        for (var state : block.getStateDefinition().getPossibleStates()) {
            if (EclipticCompat.isFullBlockState(state)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
