package com.nine.softimprints.client.profile;

import javax.annotation.Nullable;

public interface ProfileResolver {

    @Nullable
    ImprintResolveResult resolve(ImprintResolveContext context);

}
