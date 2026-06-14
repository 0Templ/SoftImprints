package com.nine.softimprints.compat.modmenu;

import com.nine.softimprints.SICommon;
import com.nine.softimprints.ui.screen.SIConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.UpdateChecker;

import java.util.Map;

public class ModMenuSetup implements ModMenuApi {

    @Override
    public Map<String, UpdateChecker> getProvidedUpdateCheckers() {
        return Map.of(
                SICommon.MODID, new SIUpdateChecker()
        );
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SIConfigScreen::new;
    }


}
