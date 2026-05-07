package com.nine.softimprints.client.core.contact;

public record ContactResult(
        ContactArea area,
        StampStrategy strategy
) {


    public enum StampStrategy {

        ELLIPSE,
        EXACT,

        ;

    }

}