package com.nine.softimprints.client.config;

import java.util.Locale;

public enum EntityTargetFilterMode {

    BLACKLIST("blacklist"),
    WHITELIST("whitelist");

    private final String id;

    EntityTargetFilterMode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static EntityTargetFilterMode fromId(String id) {
        if (id == null) {
            return BLACKLIST;
        }
        String normalized = id.toLowerCase(Locale.ROOT);
        for (EntityTargetFilterMode mode : values()) {
            if (mode.id.equals(normalized) || mode.name().equalsIgnoreCase(id)) {
                return mode;
            }
        }
        return BLACKLIST;
    }
}
