package com.nine.softimprints.client.ui.component.search;

import com.nine.softimprints.client.ui.component.list.element.AbstractConfigListEntry;
import com.nine.softimprints.client.ui.screen.SIConfigScreen;
import com.nine.softimprints.client.ui.util.constant.SIColors;
import com.nine.softimprints.client.ui.util.region.BoxRenderer;
import com.nine.softimprints.client.ui.util.region.BoxSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import java.util.*;
import java.util.function.Consumer;

public final class SearchListEntry<T> extends AbstractConfigListEntry {

    private static final Identifier SCROLLER_SPRITE =
            Identifier.withDefaultNamespace("widget/scroller");
    private static final Identifier SCROLLER_BACKGROUND_SPRITE =
            Identifier.withDefaultNamespace("widget/scroller_background");
    private static final Identifier BUTTON_SPRITE =
            Identifier.withDefaultNamespace("widget/button");
    private static final Identifier BUTTON_HIGHLIGHTED_SPRITE =
            Identifier.withDefaultNamespace("widget/button_highlighted");

    private static final int MODE_SWITCH_HEIGHT = 16;
    private static final int MODE_SWITCH_GAP = 2;
    private static final int TAB_HEIGHT = 16;
    private static final int TAB_GAP = 2;
    private static final int SEARCH_BOX_HEIGHT = 20;
    private static final int INNER_GAP = 2;

    private static final int LIST_FRAME_INSET_Y = 2;
    private static final int LIST_PADDING = 3;

    private static final int LIST_FRAME_INSET_X = 3;

    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_GAP = 2;
    private static final int ROW_BTN_WIDTH = 16;
    private static final int ROW_BTN_HEIGHT = 16;
    private static final int ROW_BTN_RIGHT_PAD = 2;
    private static final int ROW_BTN_LEFT_GAP = 2;
    private static final int ROW_PADDING_H = 2;

    private static final int ROW_HOVER_COLOR = 0x33FFFFFF;

    private enum Tab { BROWSE, SELECTED }

    /** Per-mode mutable state. Each mode owns its own selection and "Selected"-tab scroll. */
    private final class ModeState {
        final String id;
        final Component label;
        final Button switchButton;
        final Set<T> selectedValues = new LinkedHashSet<>();
        final List<SearchableEntry<T>> selectedEntries = new ArrayList<>();
        Consumer<Set<T>> changeListener = ignored -> {};
        int scrollSelected = 0;

        ModeState(String id, Component label) {
            this.id = id;
            this.label = label;
            this.switchButton = Button.builder(label, b -> switchMode(this.id))
                    .size(0, MODE_SWITCH_HEIGHT)
                    .build();
        }
    }

    private final Minecraft mc;
    private final List<SearchableEntry<T>> allEntries;
    private final List<SearchableEntry<T>> visibleEntries = new ArrayList<>();
    private final Map<String, ModeState> modes = new LinkedHashMap<>();
    private String activeModeId;
    private Consumer<String> modeChangeListener = ignored -> {};

    private final SearchListConfig config;
    private final EditBox searchBox;
    private final Button browseTab;
    private final Button selectedTab;

    private Tab activeTab = Tab.BROWSE;
    private int scrollBrowse = 0;

    private boolean scrollbarDragging = false;
    private double scrollbarGrabOffset = 0;

    public SearchListEntry(SearchListConfig config, List<? extends SearchableEntry<T>> entries) {
        super(0);
        this.mc = Minecraft.getInstance();
        this.config = config;
        this.allEntries = List.copyOf(entries);

        this.searchBox = new EditBox(mc.font, 0, 0, 0, SEARCH_BOX_HEIGHT, config.searchHint());
        this.searchBox.setMaxLength(256);
        this.searchBox.setVisible(true);
        this.searchBox.setEditable(true);
        this.searchBox.setResponder(this::onQueryChanged);

        this.browseTab = Button.builder(config.browseTabLabel(), b -> switchTab(Tab.BROWSE))
                .size(0, TAB_HEIGHT).build();
        this.selectedTab = Button.builder(config.selectedTabLabel(), b -> switchTab(Tab.SELECTED))
                .size(0, TAB_HEIGHT).build();

        syncTabActiveState();
        updateHeight(computeHeight());
    }

    /** Add a mode (id+label+initial+listener). Order of registration drives the switcher row. */
    public SearchListEntry<T> addMode(SearchListMode<T> mode) {
        Objects.requireNonNull(mode, "mode");
        ModeState state = new ModeState(mode.id(), mode.label());
        state.changeListener = mode.onChange() != null ? mode.onChange() : ignored -> {};
        modes.put(mode.id(), state);
        for (T value : mode.initialSelected()) {
            addInitialValueToMode(state, value);
        }
        if (activeModeId == null) {
            activeModeId = mode.id();
        }
        syncModeActiveState();
        updateHeight(computeHeight());
        return this;
    }

    /** Programmatically switch to a registered mode (no-op if unknown). */
    public SearchListEntry<T> setActiveMode(String id) {
        if (id == null || !modes.containsKey(id)) return this;
        if (Objects.equals(activeModeId, id)) return this;
        switchMode(id);
        return this;
    }

    /** Receives the new active mode id whenever it changes (incl. user click). */
    public SearchListEntry<T> onModeChanged(Consumer<String> listener) {
        this.modeChangeListener = listener != null ? listener : ignored -> {};
        return this;
    }

    /** Selection of the currently active mode. */
    public Set<T> selected() {
        ModeState s = activeMode();
        return s == null ? Set.of() : java.util.Collections.unmodifiableSet(s.selectedValues);
    }

    public String activeModeId() {
        return activeModeId;
    }

    private void addInitialValueToMode(ModeState state, T value) {
        if (state.selectedValues.contains(value)) return;
        for (var entry : allEntries) {
            if (entry.value().equals(value)) {
                state.selectedValues.add(value);
                state.selectedEntries.add(entry);
                return;
            }
        }
    }

    @Override
    public void updateEntryLayout(int x, int y, int width) {
        super.updateEntryLayout(x, y, width);
        updateHeight(computeHeight());
        layoutControls();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        layoutControls();

        if (hasModeSwitcher()) {
            for (ModeState ms : modes.values()) {
                ms.switchButton.extractRenderState(graphics, mouseX, mouseY, partialTick);
            }
        }
        browseTab.extractRenderState(graphics, mouseX, mouseY, partialTick);
        selectedTab.extractRenderState(graphics, mouseX, mouseY, partialTick);
        searchBox.extractRenderState(graphics, mouseX, mouseY, partialTick);

        int lx = getX();
        int ly = listAreaY();
        int lw = getWidth();
        int lh = listAreaHeight();

        int rowsY = ly + LIST_PADDING;
        int rowsH = lh - LIST_PADDING * 2;
        List<SearchableEntry<T>> rows = activeRows();
        boolean withScroll = hasScrollbar(rows);

        // Frame: vanilla menu background bleeds inside the border.
        SIConfigScreen.extractMenuBackground(
                mc, graphics,
                lx + LIST_FRAME_INSET_Y, ly, lw - LIST_FRAME_INSET_Y * 2,
                lh);
        BoxRenderer.render(graphics, BoxSkins.SECTION,
                lx, ly - LIST_FRAME_INSET_Y,
                lw, lh + LIST_FRAME_INSET_Y * 2);

        if (rows.isEmpty()) {
            renderEmptyHint(graphics, lx, ly, lw, lh);
            return;
        }

        int scrollIdx = activeScrollIndex();
        int rowsX = rowsContentX(lx);
        int rowAreaW = rowsContentWidth(lw, withScroll);
        int entryW = rowAreaW - ROW_BTN_WIDTH - ROW_BTN_RIGHT_PAD - ROW_BTN_LEFT_GAP - ROW_PADDING_H * 2;

        graphics.enableScissor(lx, rowsY, lx + lw, rowsY + rowsH);
        try {
            int last = Math.min(scrollIdx + config.visibleRows(), rows.size());
            for (int i = scrollIdx; i < last; i++) {
                renderRow(
                        graphics, rows.get(i),
                        rowsX, rowsY, rowAreaW, entryW,
                        i - scrollIdx,
                        mouseX, mouseY, partialTick
                );
            }
        } finally {
            graphics.disableScissor();
        }

        if (withScroll) {
            renderScrollbar(graphics, scrollbarX(lx + 6, lw), rowsY - 2, rowsH + 4, rows, scrollIdx);
        }
    }

    /** Left X of the row content shape (after the frame inset). */
    private static int rowsContentX(int lx) {
        return lx + LIST_FRAME_INSET_X;
    }

    /** Width of the row content shape, accounting for scrollbar reservation. */
    private static int rowsContentWidth(int lw, boolean withScroll) {
        int base = lw - LIST_FRAME_INSET_X * 2;
        return withScroll ? base - SCROLLBAR_WIDTH - SCROLLBAR_GAP : base;
    }

    /** Left X of the scrollbar sprite. */
    private static int scrollbarX(int lx, int lw) {
        return lx + lw - LIST_FRAME_INSET_X - SCROLLBAR_WIDTH;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        int button = event.button();

        if (hasModeSwitcher()) {
            for (ModeState ms : modes.values()) {
                if (ms.switchButton.mouseClicked(event, doubleClick)) return true;
            }
        }
        if (browseTab.mouseClicked(event, doubleClick)) return true;
        if (selectedTab.mouseClicked(event, doubleClick)) return true;

        boolean overSearch = isOver(mx, my, searchBox.getX(), searchBox.getY(), searchBox.getWidth(), searchBox.getHeight());
        searchBox.setFocused(overSearch);
        if (overSearch) {
            return searchBox.mouseClicked(event, doubleClick);
        }

        int ly = listAreaY();
        int lh = listAreaHeight();
        int lw = getWidth();
        if (!isOver(mx, my, getX(), ly, lw, lh)) return false;

        int rowsY = ly + LIST_PADDING;
        int rowsH = lh - LIST_PADDING * 2;
        List<SearchableEntry<T>> rows = activeRows();
        boolean withScroll = hasScrollbar(rows);

        if (button == 0 && withScroll && mx >= scrollbarX(getX() + 1, lw)) {
            scrollbarDragging = true;
            scrollbarGrabOffset = resolveScrollbarGrabOffset(my, rowsY, rowsY + rowsH, rows);
            scrollToMouse(my, rowsY, rowsY + rowsH, rows);
            return true;
        }

        int rowsX = rowsContentX(getX());
        int rowAreaW = rowsContentWidth(lw, withScroll);

        if (button == 0 && !rows.isEmpty() && isOver(mx, my, rowsX, rowsY, rowAreaW, rowsH)) {
            int scrollIdx = activeScrollIndex();
            int relY = (int) my - rowsY;
            int idx = scrollIdx + relY / config.rowHeight();
            if (idx >= 0 && idx < rows.size()) {
                int ry = rowsY + (idx - scrollIdx) * config.rowHeight();
                int btnX = rowsX + rowAreaW - ROW_BTN_WIDTH - ROW_BTN_RIGHT_PAD;
                int btnY = ry + (config.rowHeight() - ROW_BTN_HEIGHT) / 2;
                if (isOver(mx, my, btnX, btnY, ROW_BTN_WIDTH, ROW_BTN_HEIGHT)) {
                    toggleEntry(rows.get(idx));
                    playClickSound();
                    return true;
                }
            }
        }
        return false;
    }

    private void playClickSound() {
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && scrollbarDragging) {
            scrollbarDragging = false;
            scrollbarGrabOffset = 0;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (event.button() == 0 && scrollbarDragging) {
            int rowsY = listAreaY() + LIST_PADDING;
            int rowsH = listAreaHeight() - LIST_PADDING * 2;
            scrollToMouse(event.y(), rowsY, rowsY + rowsH, activeRows());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int ly = listAreaY();
        int lh = listAreaHeight();
        if (!isOver(mouseX, mouseY, getX(), ly, getWidth(), lh)) return false;

        List<SearchableEntry<T>> rows = activeRows();
        if (!hasScrollbar(rows)) return false;

        int newIdx = Mth.clamp(activeScrollIndex() - (int) Math.signum(scrollY), 0, maxScrollIndex(rows));
        setActiveScrollIndex(newIdx);
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return searchBox.isFocused() && searchBox.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return searchBox.isFocused() && searchBox.charTyped(event);
    }

    @Override
    public boolean isFocused() {
        return searchBox.isFocused();
    }


    private boolean hasModeSwitcher() {
        return modes.size() > 1;
    }

    private int modeSwitcherSlotHeight() {
        return hasModeSwitcher() ? MODE_SWITCH_HEIGHT + MODE_SWITCH_GAP : 0;
    }

    private int computeHeight() {
        // The frame visually overflows the list shape by LIST_FRAME_INSET_Y on each side,
        // so the bottom inset must be reserved as well.
        return modeSwitcherSlotHeight()
                + TAB_HEIGHT + TAB_GAP + SEARCH_BOX_HEIGHT + INNER_GAP
                + LIST_FRAME_INSET_Y + listAreaHeight() + LIST_FRAME_INSET_Y;
    }

    private int tabsRowY() {
        return getY() + modeSwitcherSlotHeight();
    }

    private int listAreaY() {
        return tabsRowY() + TAB_HEIGHT + TAB_GAP + SEARCH_BOX_HEIGHT + INNER_GAP + LIST_FRAME_INSET_Y;
    }

    private int listAreaHeight() {
        return config.visibleRows() * config.rowHeight() + LIST_PADDING * 2;
    }

    private void layoutControls() {
        int width = getWidth();
        int x = getX();

        if (hasModeSwitcher()) {
            int n = modes.size();
            int totalGap = MODE_SWITCH_GAP * (n - 1);
            int slotW = (width - totalGap) / n;
            int leftover = (width - totalGap) - slotW * n;
            int cursor = x;
            int i = 0;
            for (ModeState ms : modes.values()) {
                int w = slotW + (i < leftover ? 1 : 0);
                ms.switchButton.setX(cursor);
                ms.switchButton.setY(getY());
                ms.switchButton.setWidth(w);
                cursor += w + MODE_SWITCH_GAP;
                i++;
            }
        }

        int tabsY = tabsRowY();
        int halfW = (width - TAB_GAP) / 2;
        browseTab.setX(x);
        browseTab.setY(tabsY);
        browseTab.setWidth(halfW);
        selectedTab.setX(x + halfW + TAB_GAP);
        selectedTab.setY(tabsY);
        selectedTab.setWidth(width - halfW - TAB_GAP);

        searchBox.setX(x);
        searchBox.setY(tabsY + TAB_HEIGHT + TAB_GAP);
        searchBox.setWidth(width);
    }

    private void renderRow(
            GuiGraphicsExtractor graphics, SearchableEntry<T> entry,
            int lx, int rowsY, int rowAreaW, int entryW, int rowIndex,
            int mouseX, int mouseY, float partialTick
    ) {
        int rh = config.rowHeight();
        int ry = rowsY + rowIndex * rh;
        boolean rowHovered = mouseX >= lx && mouseX < lx + rowAreaW
                && mouseY >= ry && mouseY < ry + rh;

        if (rowHovered) {
            graphics.fill(lx, ry, lx + rowAreaW, ry + rh, ROW_HOVER_COLOR);
        }

        entry.render(
                graphics,
                lx + ROW_PADDING_H, ry,
                entryW, rh,
                mouseX, mouseY, partialTick,
                rowHovered, false
        );

        ModeState mode = activeMode();
        boolean added = mode != null && mode.selectedValues.contains(entry.value());
        int btnX = lx + rowAreaW - ROW_BTN_WIDTH - ROW_BTN_RIGHT_PAD;
        int btnY = ry + (rh - ROW_BTN_HEIGHT) / 2;
        renderRowButton(graphics, mouseX, mouseY, btnX, btnY,
                added ? "-" : "+");
    }

    private void renderRowButton(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY,
            int bx, int by, String label
    ) {
        boolean btnHovered = isOver(mouseX, mouseY, bx, by, ROW_BTN_WIDTH, ROW_BTN_HEIGHT);
        Identifier sprite = btnHovered ? BUTTON_HIGHLIGHTED_SPRITE : BUTTON_SPRITE;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, bx, by, ROW_BTN_WIDTH, ROW_BTN_HEIGHT);

        int color = btnHovered ? SIColors.WHITE : SIColors.SOFT_SOFT_GRAY;
        graphics.centeredText(mc.font, Component.literal(label),
                bx + ROW_BTN_WIDTH / 2, by + (ROW_BTN_HEIGHT - mc.font.lineHeight) / 2, color);
    }

    private void renderEmptyHint(GuiGraphicsExtractor graphics, int lx, int ly, int lw, int lh) {
        Component msg = activeTab == Tab.BROWSE
                ? (searchBox.getValue().isEmpty() ? config.typeHint() : config.emptyHint())
                : Component.translatable("gui.softimprints.search.selected_empty");
        int cx = lx + lw / 2;
        int cy = ly + (lh - mc.font.lineHeight) / 2;
        graphics.centeredText(mc.font, msg, cx, cy, SIColors.SOFT_GRAY);
    }

    private void renderScrollbar(
            GuiGraphicsExtractor graphics, int right, int ry, int rh,
            List<SearchableEntry<T>> rows, int scrollIdx
    ) {
        int barLeft = right - SCROLLBAR_WIDTH;
        int thumbH = thumbHeight(rh, rows);
        int thumbTop = thumbTop(ry, rh, rows, scrollIdx);

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_BACKGROUND_SPRITE,
                barLeft, ry, SCROLLBAR_WIDTH, rh);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE,
                barLeft, thumbTop, SCROLLBAR_WIDTH, thumbH);
    }

    private void switchTab(Tab tab) {
        if (activeTab == tab) return;
        activeTab = tab;
        scrollbarDragging = false;
        syncTabActiveState();
    }

    private void syncTabActiveState() {
        browseTab.active = activeTab != Tab.BROWSE;
        selectedTab.active = activeTab != Tab.SELECTED;
    }

    private ModeState activeMode() {
        return activeModeId == null ? null : modes.get(activeModeId);
    }

    private List<SearchableEntry<T>> activeRows() {
        if (activeTab == Tab.BROWSE) return visibleEntries;
        ModeState mode = activeMode();
        return mode == null ? List.of() : mode.selectedEntries;
    }

    private int activeScrollIndex() {
        if (activeTab == Tab.BROWSE) return scrollBrowse;
        ModeState mode = activeMode();
        return mode == null ? 0 : mode.scrollSelected;
    }

    private void setActiveScrollIndex(int idx) {
        if (activeTab == Tab.BROWSE) {
            scrollBrowse = idx;
        } else {
            ModeState mode = activeMode();
            if (mode != null) mode.scrollSelected = idx;
        }
    }

    private void toggleEntry(SearchableEntry<T> entry) {
        ModeState mode = activeMode();
        if (mode == null) return;
        T val = entry.value();
        if (mode.selectedValues.contains(val)) {
            mode.selectedValues.remove(val);
            mode.selectedEntries.removeIf(e -> e.value().equals(val));
            mode.scrollSelected = Mth.clamp(mode.scrollSelected, 0, maxScrollIndex(mode.selectedEntries));
        } else {
            mode.selectedValues.add(val);
            mode.selectedEntries.add(entry);
        }
        mode.changeListener.accept(java.util.Collections.unmodifiableSet(mode.selectedValues));
    }

    private void switchMode(String id) {
        if (id == null || !modes.containsKey(id)) return;
        if (Objects.equals(activeModeId, id)) return;
        activeModeId = id;
        scrollbarDragging = false;
        syncModeActiveState();
        modeChangeListener.accept(id);
    }

    private void syncModeActiveState() {
        for (ModeState ms : modes.values()) {
            ms.switchButton.active = !Objects.equals(ms.id, activeModeId);
        }
    }

    private void onQueryChanged(String query) {
        visibleEntries.clear();
        scrollBrowse = 0;

        String q = query.trim().toLowerCase();
        if (q.isEmpty()) return;

        for (var entry : allEntries) {
            if (entry.matches(q)) {
                visibleEntries.add(entry);
            }
        }
    }

    private boolean hasScrollbar(List<SearchableEntry<T>> rows) {
        return rows.size() > config.visibleRows();
    }

    private int maxScrollIndex(List<SearchableEntry<T>> rows) {
        return Math.max(0, rows.size() - config.visibleRows());
    }

    private int thumbHeight(int trackH, List<SearchableEntry<T>> rows) {
        if (maxScrollIndex(rows) <= 0) return trackH;
        int thumb = trackH * config.visibleRows() / rows.size();
        return Mth.clamp(thumb, 8, trackH - 4);
    }

    private int thumbTop(int ry, int trackH, List<SearchableEntry<T>> rows, int scrollIdx) {
        int max = maxScrollIndex(rows);
        if (max <= 0) return ry;
        int thumbH = thumbHeight(trackH, rows);
        int travel = trackH - thumbH;
        return ry + Mth.floor((double) scrollIdx / max * travel);
    }

    private double resolveScrollbarGrabOffset(double mouseY, int ry, int rbottom, List<SearchableEntry<T>> rows) {
        int rh = rbottom - ry;
        int thumbT = thumbTop(ry, rh, rows, activeScrollIndex());
        int thumbH = thumbHeight(rh, rows);
        return (mouseY >= thumbT && mouseY < thumbT + thumbH) ? mouseY - thumbT : thumbH / 2.0;
    }

    private void scrollToMouse(double mouseY, int ry, int rbottom, List<SearchableEntry<T>> rows) {
        int rh = rbottom - ry;
        int thumbH = thumbHeight(rh, rows);
        double track = Math.max(1, rh - thumbH);
        double rel = Mth.clamp(mouseY - ry - scrollbarGrabOffset, 0, track);
        setActiveScrollIndex(Mth.clamp((int) Math.round(rel / track * maxScrollIndex(rows)), 0, maxScrollIndex(rows)));
    }

    private static boolean isOver(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
