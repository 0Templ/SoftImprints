package com.nine.softimprints.platform;

import java.util.ServiceLoader;

public class Platform {

    public static final IPlatformCoreHelper CORE = load(IPlatformCoreHelper.class);
    public static final IPlatformSpecificHelper SPECIFIC = load(IPlatformSpecificHelper.class);

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, clazz.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }

    public static void init() {
    }
}
