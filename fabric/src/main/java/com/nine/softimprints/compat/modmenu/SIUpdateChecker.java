package com.nine.softimprints.compat.modmenu;

import com.nine.softimprints.api.meta.update.SIUpdateResult;
import com.nine.softimprints.api.meta.update.SIUpdateService;
import com.terraformersmc.modmenu.api.UpdateChecker;

public class SIUpdateChecker implements UpdateChecker {

    @Override
    public SIUpdateInfo checkForUpdates() {
        SIUpdateResult result = SIUpdateService.await();
        return SIUpdateInfo.from(result);
    }
}
