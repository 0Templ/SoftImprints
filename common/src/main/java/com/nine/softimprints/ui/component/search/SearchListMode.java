package com.nine.softimprints.ui.component.search;

import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public record SearchListMode<T>(
        String id,
        Component label,
        Collection<? extends T> initialSelected,
        Consumer<Set<T>> onChange
) {

    public static <T> SearchListMode<T> of(String id, Component label,
                                            Collection<? extends T> initialSelected,
                                            Consumer<Set<T>> onChange) {
        return new SearchListMode<>(id, label, initialSelected, onChange);
    }

    public static <T> SearchListMode<T> of(String id, Component label, Consumer<Set<T>> onChange) {
        return new SearchListMode<>(id, label, List.of(), onChange);
    }
}
