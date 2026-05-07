package com.nine.softimprints.client.config.option;

import com.nine.softimprints.client.config.ConfigSpec;
import com.nine.softimprints.client.config.LoaderTarget;

public record ConfigLoaderTarget(LoaderTarget target) implements ConfigOption {

    public static ConfigLoaderTarget of(LoaderTarget target) {
        return new ConfigLoaderTarget(target);
    }

    @Override
    public void apply(ConfigSpec spec) {
        spec.target = this.target != null ? this.target : LoaderTarget.COMMON;
    }
}
