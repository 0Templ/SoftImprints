package com.nine.softimprints.api.meta.update;

public record SIUpdateCandidate(
        SIUpdateChannel channel,
        String url,
        String version
) {}
