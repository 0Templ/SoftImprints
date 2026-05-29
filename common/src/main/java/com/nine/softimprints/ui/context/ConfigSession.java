package com.nine.softimprints.ui.context;

import com.nine.softimprints.config.ConfigDraft;
import com.nine.softimprints.config.ConfigValue;
import com.nine.softimprints.ui.draft.DraftHolder;
import com.nine.softimprints.ui.draft.SimpleDraft;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigSession {

    private final ConfigDraft configDraft = ConfigDraft.create();
    private final Map<ConfigValue<?>, DraftHolder<?>> drafts = new LinkedHashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> DraftHolder<T> draft(ConfigValue<T> config) {
        return (DraftHolder<T>) drafts.computeIfAbsent(config, raw -> createDraft((ConfigValue) raw));
    }

    public <T> T draftValue(ConfigValue<T> config) {
        return draft(config).getDraft();
    }

    public <T> boolean setDraft(ConfigValue<T> config, T value) {
        return draft(config).setDraft(config.normalize(value));
    }

    public void saveChanged() {
        drafts.forEach(this::copyHolderToConfigDraft);
        configDraft.commit();
        drafts.values().forEach(DraftHolder::applyDraft);
    }

    public void discardChanges() {
        drafts.values().forEach(DraftHolder::discardDraft);
        configDraft.resetAll();
    }

    public void restoreDefaults() {
        drafts.values().forEach(DraftHolder::restoreDefaultDraft);
    }

    private <T> DraftHolder<T> createDraft(ConfigValue<T> config) {
        return new SimpleDraft<>(
                configDraft.get(config),
                config.defaultValue(),
                value -> copyValue(config.normalize(value))
        );
    }

    @SuppressWarnings("unchecked")
    private <T> T copyValue(T value) {
        if (value instanceof List<?> list) {
            return (T) List.copyOf(list);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private <T> void copyHolderToConfigDraft(ConfigValue<?> rawConfig, DraftHolder<?> rawHolder) {
        ConfigValue<T> config = (ConfigValue<T>) rawConfig;
        DraftHolder<T> holder = (DraftHolder<T>) rawHolder;
        if (holder.hasUnsavedChanges()) {
            configDraft.set(config, holder.getDraft());
        }
    }

}
