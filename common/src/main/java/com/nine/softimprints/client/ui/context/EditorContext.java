package com.nine.softimprints.client.ui.context;

import com.nine.softimprints.client.profile.ImprintProfile;
import com.nine.softimprints.client.ui.cache.UICache;
import com.nine.softimprints.client.ui.draft.DraftHolder;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EditorContext {

    private final ProfilesSession session;
    private final ConfigSession configSession = new ConfigSession();
    private Identifier selected;

    private final List<Runnable> selectedProfileListeners = new ArrayList<>();
    private final List<Runnable> selectedProfileDraftListeners = new ArrayList<>();
    private final List<Runnable> onCloseListeners = new ArrayList<>();

    private final Runnable selectedDraftRelay = this::notifySelectedProfileDraftChanged;

    public EditorContext(ProfilesSession session) {
        this.session = session;
        this.selected = session.draftOrFirst(UICache.selectedProfile()).getDraft().id;
        currentProfile().addListener(selectedDraftRelay);
    }

    public ProfilesSession session() {
        return session;
    }

    public ConfigSession config() {
        return configSession;
    }

    public void saveChanged() {
        session.saveChanged();
        configSession.saveChanged();
    }

    public void resetAllDrafts() {
        session.drafts().forEach(DraftHolder::restoreDefaultDraft);
        configSession.restoreDefaults();
    }

    public int totalProfiles() {
        return session.ids().size();
    }

    public void switchProfile(boolean forward) {
        setCurrentProfile(forward ? session.nextId(selected) : session.previousId(selected));
    }

    public void setCurrentProfile(Identifier next) {
        if (Objects.equals(selected, next)) return;
        var prevProfile = session.draft(selected);
        if (prevProfile != null) {
            prevProfile.removeListener(selectedDraftRelay);
        }
        selected = next;
        var curProfile = session.draft(next);

        if (curProfile == null) {
            return;
        }
        curProfile.addListener(selectedDraftRelay);

        notifySelectedProfileChanged();
        notifySelectedProfileDraftChanged();

        UICache.setSelectedProfileId(this.selected);
    }

    public Identifier currentProfileId() {
        return selected;
    }

    public DraftHolder<ImprintProfile> currentProfile() {
        var ret = session.draftOrFirst(selected);
        if (ret == null) {
            throw new IllegalStateException("Selected profile draft is not initialized");
        }
        return ret;
    }

    public void addSelectedProfileListener(Runnable listener) {
        selectedProfileListeners.add(listener);
    }

    public void addSelectedProfileDraftListener(Runnable listener) {
        selectedProfileDraftListeners.add(listener);
    }

    public void addOnCloseListeners(Runnable listener) {
        onCloseListeners.add(listener);
    }

    private void notifySelectedProfileChanged() {
        new ArrayList<>(selectedProfileListeners).forEach(Runnable::run);
    }

    private void notifySelectedProfileDraftChanged() {
        new ArrayList<>(selectedProfileDraftListeners).forEach(Runnable::run);
    }

    public void notifyOnCloseListeners() {
        new ArrayList<>(onCloseListeners).forEach(Runnable::run);
    }

}
