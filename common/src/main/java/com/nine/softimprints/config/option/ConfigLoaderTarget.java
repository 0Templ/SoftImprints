package com.nine.softimprints.config.option;

import com.nine.softimprints.config.ConfigSpec;
import com.nine.softimprints.config.LoaderTarget;

public record ConfigLoaderTarget(LoaderTarget target) implements ConfigOption {

    public static ConfigLoaderTarget of(LoaderTarget target) {
        return new ConfigLoaderTarget(target);
    }

    @Override
    public void apply(ConfigSpec spec) {
        spec.target = this.target != null ? this.target : LoaderTarget.COMMON;
    }
}
