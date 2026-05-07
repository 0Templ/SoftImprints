package com.nine.softimprints.client.ui.draft;

import com.nine.softimprints.client.profile.ImprintProfile;

public class ImprintProfileDraft extends SimpleDraft<ImprintProfile> {

    public ImprintProfileDraft(ImprintProfile profile, ImprintProfile defaultProfile) {
        super(profile.copy(), defaultProfile.copy(), ImprintProfile::copy);
    }


}
