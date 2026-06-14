package com.nine.softimprints.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import java.util.*;

public final class ConfigDraft {

    private final Map<ConfigValue<?>, Object> draftValues = new LinkedHashMap<>();

    private ConfigDraft() {
        this.resetAll();
    }

    public static ConfigDraft create() {
        return new ConfigDraft();
    }

    private static void applyPendingChanges(
            com.electronwill.nightconfig.core.Config view,
            List<PendingChange> changes
    ) {
        for (PendingChange change : changes) {
            writeRawValue(view, change.path(), change.value());
        }
    }

    private static void writeRawValue(
            com.electronwill.nightconfig.core.Config root,
            String path,
            Object value
    ) {
        String[] segments = path.split("\\.");
        com.electronwill.nightconfig.core.Config current = root;
        for (int i = 0; i < segments.length - 1; i++) {
            String segment = segments[i];
            Object next = current.valueMap().get(segment);
            if (next instanceof com.electronwill.nightconfig.core.Config nextConfig) {
                current = nextConfig;
                continue;
            }

            com.electronwill.nightconfig.core.Config created = current.createSubConfig();
            current.valueMap().put(segment, created);
            current = created;
        }
        current.valueMap().put(segments[segments.length - 1], value);
    }

    public void resetAll() {
        this.draftValues.clear();
        for (ConfigValue<?> configValue : ConfigImpl.registeredValues()) {
            this.draftValues.put(configValue, configValue.get());
        }
    }

    public void resetAllToDefaults() {
        this.draftValues.clear();
        for (ConfigValue<?> configValue : ConfigImpl.registeredValues()) {
            this.draftValues.put(configValue, configValue.defaultValue());
        }
    }

    public <T> T get(ConfigValue<T> configValue) {
        Object draftValue = this.draftValues.get(configValue);
        return draftValue != null ? configValue.cast(draftValue) : configValue.get();
    }

    public <T> void set(
            ConfigValue<T> configValue,
            T value
    ) {
        this.draftValues.put(configValue, configValue.normalize(value));
    }

    public <T> void reset(ConfigValue<T> configValue) {
        this.draftValues.put(configValue, configValue.get());
    }

    public <T> boolean isDirty(ConfigValue<T> configValue) {
        return !Objects.equals(configValue.get(), this.get(configValue));
    }

    public void commit() {
        Map<CommentedFileConfig, List<PendingChange>> changesByConfig = new LinkedHashMap<>();
        for (ConfigValue<?> configValue : ConfigImpl.registeredValues()) {
            this.collectPendingChange(configValue, changesByConfig);
        }

        for (Map.Entry<CommentedFileConfig, List<PendingChange>> entry : changesByConfig.entrySet()) {
            CommentedFileConfig config = entry.getKey();
            List<PendingChange> changes = entry.getValue();
            if (config == null || changes.isEmpty()) {
                continue;
            }

            applyPendingChanges(config, changes);
            config.save();
        }
    }

    private <T> void collectPendingChange(
            ConfigValue<T> configValue,
            Map<CommentedFileConfig, List<PendingChange>> changesByConfig
    ) {
        T draftValue = this.get(configValue);
        if (!Objects.equals(configValue.get(), draftValue)) {
            CommentedFileConfig config = configValue.config();
            if (config == null) {
                return;
            }
            changesByConfig.computeIfAbsent(config, ignored -> new ArrayList<>(8))
                    .add(new PendingChange(configValue.path(), configValue.serializeNormalized(draftValue)));
        }
    }

    private record PendingChange(String path, Object value) {
    }
}
