package com.nine.softimprints.config.option;

import com.nine.softimprints.config.ConfigSpec;

public interface ConfigOption {

    void apply(ConfigSpec spec);
}
