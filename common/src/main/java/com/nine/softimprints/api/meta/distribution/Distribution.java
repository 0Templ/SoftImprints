package com.nine.softimprints.api.meta.distribution;

public enum Distribution {

    MODRINTH("modrinth"),
    CURSEFORGE("curseforge"),
    GITHUB("github"),
    UNKNOWN("unknown");

    ;

    private final String labelKey;
    private final String key;

    Distribution(String id){
        this.key = id;
        this.labelKey = "config.softimprints.group.info.distribution." + id;
    }

    public String getLabelKey(){
        return labelKey;
    }

    public static Distribution fromJsonKey(String key) {
        for (Distribution distribution : values()) {
            if (distribution.key.equalsIgnoreCase(key)) {
                return distribution;
            }
        }

        return null;
    }


}