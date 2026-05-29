package com.nine.softimprints.profile.resolver;

import javax.annotation.Nullable;

public interface ProfileResolver {

    @Nullable
    ImprintResolveResult resolve(ImprintResolveContext context);

}
