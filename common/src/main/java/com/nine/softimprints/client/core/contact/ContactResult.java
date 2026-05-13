package com.nine.softimprints.client.core.contact;

import com.nine.softimprints.client.core.contact.bounds.CompositeContactShape;

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