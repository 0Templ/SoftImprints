package com.nine.softimprints.profile.options.resolution;

public record ImprintResolution(int mapSize) {


    public static ImprintResolution DEFAULT = new ImprintResolution(16);

    public ImprintResolution withMapSize(int val){
        return new ImprintResolution(val);
    }

}
