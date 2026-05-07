package com.nine.softimprints.client.ui.component.search;

import net.minecraft.network.chat.Component;

public record SearchListConfig(
        int rowHeight,
        int visibleRows,
        Component searchHint,
        Component typeHint,
        Component emptyHint,
        Component browseTabLabel,
        Component selectedTabLabel
) {

    public static SearchListConfig defaults(int rowHeight, int visibleRows) {
        return new SearchListConfig(
                rowHeight,
                visibleRows,
                Component.translatable("gui.softimprints.search.search_hint"),
                Component.translatable("gui.softimprints.search.type_something"),
                Component.translatable("gui.softimprints.search.no_results"),
                Component.translatable("gui.softimprints.search.tab.browse"),
                Component.translatable("gui.softimprints.search.tab.selected")
        );
    }
}
