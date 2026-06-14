package com.nine.softimprints.ui.context;

import com.nine.softimprints.profile.ImprintProfile;
import com.nine.softimprints.profile.catalog.entry.ImprintProfileEntry;
import com.nine.softimprints.ui.cache.UICache;
import com.nine.softimprints.ui.draft.DraftHolder;
import net.minecraft.resources.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EditorContext {

    private final ProfilesSession session;
    private final ConfigSession configSession = new ConfigSession();
    private final List<Runnable> selectedProfileListeners = new ArrayList<>();
    private final List<Runnable> selectedProfileDraftListeners = new ArrayList<>();
    private final List<Runnable> onCloseListeners = new ArrayList<>();
    private final Runnable selectedDraftRelay = this::notifySelectedProfileDraftChanged;
    private Identifier selected;

    public EditorContext(ProfilesSession session) {
        this.session = session;
        this.selected = session.entryOrFirst(UICache.selectedProfile()).id();

        var draft = currentDraft();
        if (draft != null) {
            draft.addListener(selectedDraftRelay);
        }
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

    public void setCurrent(Identifier next) {
        if (Objects.equals(selected, next)) return;
        var prevDraft = session.draft(selected);
        if (prevDraft != null) {
            prevDraft.removeListener(selectedDraftRelay);
        }
        selected = next;
        UICache.setSelectedProfileId(selected);

        var curDraft = session.draft(selected);
        if (curDraft != null) {
            curDraft.addListener(selectedDraftRelay);
        }

        notifySelectedProfileChanged();
        notifySelectedProfileDraftChanged();
    }

    public Identifier currentId() {
        return selected;
    }

    public ImprintProfileEntry currentEntry() {
        return session.entry(selected);
    }

    @Nullable
    public ImprintProfile currentProfile() {
        var draft = currentDraft();
        return draft == null ? null : draft.getDraft();
    }

    @Nullable
    public DraftHolder<ImprintProfile> currentDraft() {
        return session.draft(selected);
    }

    @Nonnull
    public DraftHolder<ImprintProfile> requireCurrentDraft() {
        var draft = currentDraft();
        if (draft == null) {
            throw new IllegalStateException("Selected profile is not editable: " + selected);
        }
        return draft;
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
