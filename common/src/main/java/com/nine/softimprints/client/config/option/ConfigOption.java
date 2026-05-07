package com.nine.softimprints.client.config.option;

import com.nine.softimprints.client.config.ConfigSpec;

public interface ConfigOption {

    void apply(ConfigSpec spec);
}
