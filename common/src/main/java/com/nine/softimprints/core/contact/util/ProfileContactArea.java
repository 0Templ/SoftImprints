package com.nine.softimprints.core.contact.util;

import com.nine.softimprints.core.contact.bounds.CompositeContactShape;
import com.nine.softimprints.profile.ImprintProfile;

//Todo : move where?
public record ProfileContactArea(
        ImprintProfile profile,
        CompositeContactShape shape
) {

}
