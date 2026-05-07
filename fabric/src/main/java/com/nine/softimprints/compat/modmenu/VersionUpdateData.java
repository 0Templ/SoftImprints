package com.nine.softimprints.compat.modmenu;

import com.terraformersmc.modmenu.api.UpdateChannel;

public record VersionUpdateData(UpdateChannel updateChannel, String url, String version) {

}
