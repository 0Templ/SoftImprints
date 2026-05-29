package com.nine.softimprints.profile.catalog.entry.exception;

import com.nine.softimprints.profile.catalog.entry.issue.ProfileIssue;
import com.nine.softimprints.profile.catalog.entry.issue.UnsupportedSchemaIssue;

public class UnsupportedProfileSchemaException extends ProfileLoadException {

    private final int foundVersion;
    private final int supportedVersion;

    public UnsupportedProfileSchemaException(int foundVersion, int supportedVersion) {
        super("Profile schema " + foundVersion + " is newer than supported " + supportedVersion);
        this.foundVersion = foundVersion;
        this.supportedVersion = supportedVersion;
    }

    @Override
    public ProfileIssue toIssue() {
        return new UnsupportedSchemaIssue(foundVersion, supportedVersion);
    }
}
