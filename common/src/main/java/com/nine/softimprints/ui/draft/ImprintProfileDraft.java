package com.nine.softimprints.ui.draft;

import com.nine.softimprints.profile.ImprintProfile;

public class ImprintProfileDraft extends SimpleDraft<ImprintProfile> {

    public ImprintProfileDraft(ImprintProfile profile, ImprintProfile defaultProfile) {
        super(profile.copy(), defaultProfile.copy(), ImprintProfile::copy);
    }


}
