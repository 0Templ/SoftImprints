package com.nine.softimprints.config;

public enum ModelContactFallbackPolicy {
    SKIP("skip"),
    BOUNDING_BOX("bounding_box"),
    LAST_SNAPSHOT("last_snapshot");

    private final String id;

    ModelContactFallbackPolicy(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

}
