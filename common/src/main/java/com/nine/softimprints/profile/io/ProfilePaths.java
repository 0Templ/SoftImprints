package com.nine.softimprints.profile.io;

import net.minecraft.resources.Identifier;

import java.nio.file.Path;

public class ProfilePaths {

    public static Path toConfigPath(Path configBase, Identifier id) {
        return configBase.resolve(id.getNamespace()).resolve(id.getPath() + ".json");
    }

    public static String toResourceRelative(String folder, Identifier id) {
        return folder + "/" + id.getPath() + ".json";
    }

}
