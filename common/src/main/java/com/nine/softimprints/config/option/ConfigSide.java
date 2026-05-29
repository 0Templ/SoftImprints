package com.nine.softimprints.config.option;

import com.nine.softimprints.config.ConfigSpec;

public enum ConfigSide implements ConfigOption {

    CLIENT,

    COMMON;

    @Override
    public void apply(ConfigSpec spec) {
        spec.side = this;
    }
}
