package com.nine.softimprints.ui.component.search;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class SearchListBuilder<T> {

    private static final String DEFAULT_MODE_ID = "default";

    private final List<SearchableEntry<T>> entries = new ArrayList<>();
    private int rowHeight = 20;
    private int visibleRows = 6;
    private SearchListEntry.Tab defaultTab;
    private Component searchHint;
    private Component typeHint;
    private Component emptyHint;
    private Component browseTabLabel;
    private Component selectedTabLabel;

    private Consumer<Set<T>> singleModeListener = ignored -> {};
    private final List<T> singleModeInitial = new ArrayList<>();

    private final List<SearchListMode<T>> modes = new ArrayList<>();
    private String initialActiveModeId;
    private Consumer<String> modeChangeListener;

    private SearchListBuilder() {}

    public static <T> SearchListBuilder<T> create() {
        return new SearchListBuilder<>();
    }

    public SearchListBuilder<T> addAll(Collection<? extends SearchableEntry<T>> entries) {
        this.entries.addAll(entries);
        return this;
    }

    public SearchListBuilder<T> rowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
        return this;
    }

    public SearchListBuilder<T> visibleRows(int visibleRows) {
        this.visibleRows = visibleRows;
        return this;
    }

    public SearchListBuilder<T> searchHint(Component hint) {
        this.searchHint = hint;
        return this;
    }

    public SearchListBuilder<T> typeHint(Component hint) {
        this.typeHint = hint;
        return this;
    }

    public SearchListBuilder<T> emptyHint(Component hint) {
        this.emptyHint = hint;
        return this;
    }

    public SearchListBuilder<T> browseTabLabel(Component label) {
        this.browseTabLabel = label;
        return this;
    }

    public SearchListBuilder<T> selectedTabLabel(Component label) {
        this.selectedTabLabel = label;
        return this;
    }

    public SearchListBuilder<T> onChange(Consumer<Set<T>> listener) {
        this.singleModeListener = listener != null ? listener : ignored -> {};
        return this;
    }

    public SearchListBuilder<T> selectInitial(Collection<? extends T> values) {
        this.singleModeInitial.addAll(values);
        return this;
    }

    public SearchListBuilder<T> addMode(SearchListMode<T> mode) {
        this.modes.add(mode);
        return this;
    }

    public SearchListBuilder<T> initialMode(String id) {
        this.initialActiveModeId = id;
        return this;
    }

    public SearchListBuilder<T> onModeChanged(Consumer<String> listener) {
        this.modeChangeListener = listener;
        return this;
    }

    public SearchListBuilder<T> initDefaultTab(SearchListEntry.Tab tab) {
        this.defaultTab = tab;
        return this;
    }

    public SearchListEntry<T> build() {
        var defaults = SearchListConfig.defaults(rowHeight, visibleRows);
        var config = new SearchListConfig(
                defaults.rowHeight(),
                defaults.visibleRows(),
                defaultTab != null ? defaultTab : defaults.defaultTab(),
                searchHint != null ? searchHint : defaults.searchHint(),
                typeHint != null ? typeHint : defaults.typeHint(),
                emptyHint != null ? emptyHint : defaults.emptyHint(),
                browseTabLabel != null ? browseTabLabel : defaults.browseTabLabel(),
                selectedTabLabel != null ? selectedTabLabel : defaults.selectedTabLabel()
        );
        SearchListEntry<T> entry = new SearchListEntry<>(config, entries);

        if (modes.isEmpty()) {
            entry.addMode(SearchListMode.of(
                    DEFAULT_MODE_ID, Component.empty(),
                    singleModeInitial, singleModeListener
            ));
        } else {
            for (SearchListMode<T> mode : modes) {
                entry.addMode(mode);
            }
            if (initialActiveModeId != null) {
                entry.setActiveMode(initialActiveModeId);
            }
            if (modeChangeListener != null) {
                entry.onModeChanged(modeChangeListener);
            }
        }
        return entry;
    }
}
