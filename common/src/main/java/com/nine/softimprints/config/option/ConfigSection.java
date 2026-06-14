package com.nine.softimprints.config.option;

import com.nine.softimprints.config.ConfigSpec;

public enum ConfigSection implements ConfigOption {

    NONE(""),
    GENERAL("general"),
    PLUGINS("plugins"),

    ;

    public final String key;
    public final ConfigSection parent;
    ConfigSection(String key) {
        this(null, key);
    }

    ConfigSection(
            ConfigSection parent,
            String key
    ) {
        this.parent = parent;
        this.key = key;
    }

    @Override
    public void apply(ConfigSpec spec) {
        spec.section = this;
    }

    public String getFullKey() {
        return key;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return key;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public ConfigSection getParent() {
        return parent;
    }
}
