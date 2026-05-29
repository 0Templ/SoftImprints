package com.nine.softimprints;

import com.nine.softimprints.ui.screen.SIConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(SICommon.MODID)
public class SINeoForge {

    public SINeoForge(IEventBus modEventBus, Dist dist, ModContainer container) {
        SICommon.init();

        if (dist.isClient()) bindConfigScreen(container);
    }

    private void bindConfigScreen(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parent) -> new SIConfigScreen(parent));
    }



}
