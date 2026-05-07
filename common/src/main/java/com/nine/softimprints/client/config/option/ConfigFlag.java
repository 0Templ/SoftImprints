package com.nine.softimprints.client.config.option;

import com.nine.softimprints.client.config.ConfigSpec;

public enum ConfigFlag implements ConfigOption {

    SYNC {
        @Override
        public void apply(ConfigSpec spec) {
            spec.shouldSync = true;
        }
    },
    HIDE_CONSTRAINTS {
        @Override
        public void apply(ConfigSpec spec) {
            spec.hideConstraints = true;
        }
    }
}
