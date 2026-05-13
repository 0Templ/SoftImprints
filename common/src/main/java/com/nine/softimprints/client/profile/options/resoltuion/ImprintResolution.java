package com.nine.softimprints.client.profile.options.resoltuion;

public record ImprintResolution(int mapSize, int textureSize) {



    public static ImprintResolution DEFAULT = new ImprintResolution(16, 16);

}
