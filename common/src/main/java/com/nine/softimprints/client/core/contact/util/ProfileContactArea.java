package com.nine.softimprints.client.core.contact.util;

import com.nine.softimprints.client.core.contact.bounds.CompositeContactShape;
import com.nine.softimprints.client.profile.ImprintProfile;

//Todo : move where?
public record ProfileContactArea(
        ImprintProfile profile,
        CompositeContactShape shape
) {

}
