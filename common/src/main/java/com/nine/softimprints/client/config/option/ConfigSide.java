package com.nine.softimprints.client.config.option;

import com.nine.softimprints.client.config.ConfigSpec;

public enum ConfigSide implements ConfigOption {

    CLIENT,

    COMMON;

    @Override
    public void apply(ConfigSpec spec) {
        spec.side = this;
    }
}
