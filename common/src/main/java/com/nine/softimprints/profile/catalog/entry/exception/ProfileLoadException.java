package com.nine.softimprints.profile.catalog.entry.exception;

import com.nine.softimprints.profile.catalog.entry.issue.ProfileIssue;

public abstract class ProfileLoadException extends RuntimeException {

    public ProfileLoadException(String message) {
        super(message);
    }

    public ProfileLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract ProfileIssue toIssue();


}