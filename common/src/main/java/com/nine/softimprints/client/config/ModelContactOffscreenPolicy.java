package com.nine.softimprints.client.config;

import java.util.Locale;

public enum ModelContactOffscreenPolicy {
    DISABLED("disabled"),
    ON_DEMAND("on_demand");

    private final String id;

    ModelContactOffscreenPolicy(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static ModelContactOffscreenPolicy fromId(String id) {
        if (id == null) {
            return ON_DEMAND;
        }
        String normalized = id.toLowerCase(Locale.ROOT);
        for (ModelContactOffscreenPolicy policy : values()) {
            if (policy.id.equals(normalized) || policy.name().equalsIgnoreCase(id)) {
                return policy;
            }
        }
        return ON_DEMAND;
    }
}
