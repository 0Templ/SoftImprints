package com.nine.softimprints.api.meta.update;

import com.nine.softimprints.api.meta.distribution.Distribution;

import java.util.Map;

public record SIUpdateResult(
        SIUpdateStatus status,
        Map<Distribution, SIUpdateCandidate> candidates
) {

    public static final SIUpdateResult CHECKING = new SIUpdateResult(SIUpdateStatus.CHECKING, null);
    public static final SIUpdateResult NONE = new SIUpdateResult(SIUpdateStatus.NONE, null);
    public static final SIUpdateResult FAILED = new SIUpdateResult(SIUpdateStatus.FAILED, null);

    public static SIUpdateResult available(Map<Distribution, SIUpdateCandidate> candidates) {
        return new SIUpdateResult(SIUpdateStatus.AVAILABLE, candidates);
    }

    public boolean updateAvailable() {
        return status == SIUpdateStatus.AVAILABLE && candidates != null && !candidates.isEmpty();
    }
}
