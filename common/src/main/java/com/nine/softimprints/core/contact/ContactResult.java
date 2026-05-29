package com.nine.softimprints.core.contact;

import com.nine.softimprints.core.contact.bounds.CompositeContactShape;

public record ContactResult(
        CompositeContactShape shape,
        StampStrategy strategy
) {


    public enum StampStrategy {

        ELLIPSE,
        EXACT,

        ;
    }

}