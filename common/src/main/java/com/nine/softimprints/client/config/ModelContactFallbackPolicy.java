package com.nine.softimprints.client.config;

import java.util.Locale;

public enum ModelContactFallbackPolicy {
    SKIP("skip"),
    BOUNDING_BOX("bounding_box");

    private final String id;

    ModelContactFallbackPolicy(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static ModelContactFallbackPolicy fromId(String id) {
        if (id == null) {
            return SKIP;
        }
        String normalized = id.toLowerCase(Locale.ROOT);
        for (ModelContactFallbackPolicy policy : values()) {
            if (policy.id.equals(normalized) || policy.name().equalsIgnoreCase(id)) {
                return policy;
            }
        }
        return SKIP;
    }
}
