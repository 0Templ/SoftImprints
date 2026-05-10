package com.nine.softimprints;

import com.nine.softimprints.client.config.SIConfig;
import com.nine.softimprints.client.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SICommon {


    public static final String MODID = "softimprints";

    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static void init(){
        SIConfig.init();
        Platform.init();
    }

}
