package com.nine.softimprints.client.ui.component.list;

import com.nine.softimprints.client.ui.component.list.element.ConfigListEntry;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class ConfigListWidget extends AbstractWidget {

    private static final int ELEMENT_SIDE_PADDING = 5;
    private static final int ELEMENT_SPACING = 4;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_GAP = 2;

    private static final Identifier SCROLLER_SPRITE = Identifier.withDefaultNamespace("widget/scroller");
    private static final Identifier SCROLLER_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("widget/scroller_background");

    private final List<ListGroup> groups = new ArrayList<>();

    private int activeGroupIndex = -1;
    private boolean layoutDirty = true;
    private int lastLayoutX = Integer.MIN_VALUE;
    private int lastLayoutY = Integer.MIN_VALUE;
    private int lastLayoutWidth = Integer.MIN_VALUE;
    private int lastLayoutHeight = Integer.MIN_VALUE;
    private int lastGroupIndex = Integer.MIN_VALUE;

    private double scrollAmount;
    private boolean scrolling;
    private double scrollbarGrabOffset;
    private ConfigListEntry activeMouseEntry;
    private ConfigListEntry focusedEntry;
    private int contentHeight;
    private int contentWidth;

    public ConfigListWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    public void addGroup(ListGroup group) {
        if (this.groups.contains(group)) {
            return;
        }
        group.attachToList(this);
        this.groups.add(group);
        if (this.activeGroupIndex < 0) {
            this.activeGroupIndex = 0;
        }

        this.invalidateLayout();
    }

    public void setGroups(List<ListGroup> groups, int activeGroupIndex) {
        this.groups.clear();
        for (ListGroup group : groups) {
            group.attachToList(this);
            this.groups.add(group);
        }

        this.activeGroupIndex = this.groups.isEmpty()
                ? -1
                : Mth.clamp(activeGroupIndex, 0, this.groups.size() - 1);
        this.scrollAmount = 0.0D;
        this.scrolling = false;
        this.scrollbarGrabOffset = 0.0D;
        this.activeMouseEntry = null;
        this.setFocusedEntry(null);
        this.invalidateLayout();
    }

    public int getGroupCount() {
        return this.groups.size();
    }

    public ListGroup getGroup(int index) {
        if (index < 0 || index >= this.groups.size()) {
            throw new IllegalArgumentException("Invalid group index: " + index);
        }
        return this.groups.get(index);
    }

    public void setActiveGroup(int groupIndex) {
        if (groupIndex < 0 || groupIndex >= this.groups.size() || this.activeGroupIndex == groupIndex) {
            return;
        }
        this.activeGroupIndex = groupIndex;
        this.scrollAmount = 0.0D;
        this.scrolling = false;
        this.scrollbarGrabOffset = 0.0D;
        this.activeMouseEntry = null;
        this.setFocusedEntry(null);
        this.invalidateLayout();
    }

    public void setActiveGroup(ListGroup group) {
        int index = this.groups.indexOf(group);
        if (index >= 0) {
            this.setActiveGroup(index);
        }
    }

    public ListGroup getActiveGroup() {
        if (this.activeGroupIndex < 0 || this.activeGroupIndex >= this.groups.size()) {
            return null;
        }
        return this.groups.get(this.activeGroupIndex);
    }

    public int getActiveGroupIndex() {
        return this.activeGroupIndex;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.ensureLayout();

        int left = this.getX();
        int top = this.getY();
        int right = left + this.getWidth();
        int bottom = top + this.getHeight();
        int contentLeft = left + ELEMENT_SIDE_PADDING;
        int contentRight = contentLeft + this.contentWidth;

        for (ConfigListEntry entry : this.activeEntries()) {
            int entryBottom = entry.getY() + entry.getHeight();
            if (entryBottom < top || entry.getY() > bottom) {
                continue;
            }

            graphics.enableScissor(contentLeft, top, contentRight, bottom);
            entry.render(graphics, mouseX, mouseY, partialTick);
            graphics.disableScissor();
        }

        this.renderScrollbar(graphics, right, top, bottom);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.ensureLayout();
        for (ConfigListEntry entry : this.activeEntries()) {
            entry.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!this.active || !this.visible) {
            return false;
        }

        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        if (button == 0) {
            this.scrolling = false;
            this.scrollbarGrabOffset = 0.0D;
            this.activeMouseEntry = null;
        }

        int left = this.getX();
        int top = this.getY();
        int right = left + this.getWidth();
        int bottom = top + this.getHeight();
        if (mouseX < left || mouseX >= right || mouseY < top || mouseY >= bottom) {
            return false;
        }

        this.ensureLayout();

        if (button == 0 && this.isScrollbarVisible()) {
            int scrollbarLeft = right - SCROLLBAR_WIDTH;
            if (mouseX >= scrollbarLeft && mouseX < right) {
                this.scrolling = true;
                this.scrollbarGrabOffset = this.resolveScrollbarGrabOffset(mouseY, top, bottom);
                this.scrollToMouse(mouseY, top, bottom);
                return true;
            }
        }

        int contentLeft = left + ELEMENT_SIDE_PADDING;
        int contentRight = contentLeft + this.contentWidth;
        if (mouseX < contentLeft || mouseX >= contentRight) {
            return false;
        }

        for (ConfigListEntry entry : this.activeEntries()) {
            int entryBottom = entry.getY() + entry.getHeight();
            if (entryBottom < top || entry.getY() > bottom) {
                continue;
            }
            if (entry.mouseClicked(event, doubleClick)) {
                if (button == 0) {
                    this.setFocusedEntry(entry);
                    this.activeMouseEntry = entry;
                    this.setFocused(true);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && this.scrolling) {
            this.scrolling = false;
            this.scrollbarGrabOffset = 0.0D;
            return true;
        }

        ConfigListEntry activeEntry = this.activeMouseEntry;
        if (event.button() == 0) {
            this.activeMouseEntry = null;
        }

        this.ensureLayout();
        return activeEntry != null && activeEntry.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (event.button() == 0 && this.scrolling) {
            this.scrollToMouse(event.y(), this.getY(), this.getY() + this.getHeight());
            return true;
        }

        this.ensureLayout();
        return this.activeMouseEntry != null && this.activeMouseEntry.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int left = this.getX();
        int top = this.getY();
        int right = left + this.getWidth();
        int bottom = top + this.getHeight();

        if (mouseX < left || mouseX >= right || mouseY < top || mouseY >= bottom) {
            return false;
        }

        this.ensureLayout();
        for (ConfigListEntry entry : this.activeEntries()) {
            int entryBottom = entry.getY() + entry.getHeight();
            if (entryBottom < top || entry.getY() > bottom) {
                continue;
            }
            if (entry.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }

        this.setScrollAmount(this.scrollAmount - scrollY * 12.0D);
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        this.ensureLayout();
        return this.focusedEntry != null && this.focusedEntry.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        this.ensureLayout();
        return this.focusedEntry != null && this.focusedEntry.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        this.ensureLayout();
        return this.focusedEntry != null && this.focusedEntry.charTyped(event);
    }

    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
        this.ensureLayout();
        List<ConfigListEntry> entries = this.focusableEntries();
        if (entries.isEmpty()) {
            return null;
        }

        int current = this.focusedEntry != null ? entries.indexOf(this.focusedEntry) : -1;

        if (navigationEvent instanceof FocusNavigationEvent.ArrowNavigation arrow) {
            ScreenDirection dir = arrow.direction();
            if (dir == ScreenDirection.UP || dir == ScreenDirection.DOWN) {
                if (current < 0) {
                    return pathFor(entries.get(dir == ScreenDirection.DOWN ? 0 : entries.size() - 1));
                }
                int next = current + (dir == ScreenDirection.DOWN ? 1 : -1);
                if (next < 0 || next >= entries.size()) {
                    return null;
                }
                return pathFor(entries.get(next));
            }
            return pathFor(current >= 0 ? entries.get(current) : entries.get(0));
        }

        if (navigationEvent instanceof FocusNavigationEvent.TabNavigation tab) {
            boolean forward = tab.forward();
            if (current < 0) {
                return pathFor(entries.get(forward ? 0 : entries.size() - 1));
            }
            int next = current + (forward ? 1 : -1);
            if (next < 0 || next >= entries.size()) {
                return null;
            }
            return pathFor(entries.get(next));
        }

        return null;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.setFocusedEntry(null);
        }
    }

    public void invalidateLayout() {
        this.layoutDirty = true;
    }

    private ComponentPath pathFor(ConfigListEntry entry) {
        this.setFocusedEntry(entry);
        this.scrollEntryIntoView(entry);
        return ComponentPath.leaf(this);
    }

    private void setFocusedEntry(ConfigListEntry entry) {
        if (this.focusedEntry == entry) {
            return;
        }

        if (this.focusedEntry != null) {
            this.focusedEntry.setFocused(false);
        }

        this.focusedEntry = entry;

        if (this.focusedEntry != null) {
            this.focusedEntry.setFocused(true);
        }
    }

    private List<ConfigListEntry> focusableEntries() {
        List<ConfigListEntry> result = new ArrayList<>();
        for (ConfigListEntry entry : this.activeEntries()) {
            if (entry.isFocusable()) {
                result.add(entry);
            }
        }
        return result;
    }

    private void scrollEntryIntoView(ConfigListEntry entry) {
        int viewTop = this.getY();
        int viewBottom = viewTop + this.getHeight();
        int entryTop = entry.getY();
        int entryBottom = entryTop + entry.getHeight();

        if (entryTop < viewTop) {
            this.setScrollAmount(this.scrollAmount - (viewTop - entryTop));
        } else if (entryBottom > viewBottom) {
            this.setScrollAmount(this.scrollAmount + (entryBottom - viewBottom));
        }
    }

    private void ensureLayout() {
        if (!this.layoutDirty
                && this.lastLayoutX == this.getX()
                && this.lastLayoutY == this.getY()
                && this.lastLayoutWidth == this.getWidth()
                && this.lastLayoutHeight == this.getHeight()
                && this.lastGroupIndex == this.activeGroupIndex) {
            return;
        }

        int viewportHeight = Math.max(0, this.getHeight());
        int baseContentWidth = Math.max(0, this.getWidth() - ELEMENT_SIDE_PADDING * 2);

        this.contentWidth = baseContentWidth;
        this.contentHeight = this.layoutEntries(this.contentWidth);

        if (this.contentHeight > viewportHeight) {
            this.contentWidth = Math.max(0, baseContentWidth - SCROLLBAR_WIDTH - SCROLLBAR_GAP);
            this.contentHeight = this.layoutEntries(this.contentWidth);
        }

        double clampedScroll = Mth.clamp(this.scrollAmount, 0.0D, this.getMaxScroll());
        if (clampedScroll != this.scrollAmount) {
            this.scrollAmount = clampedScroll;
            this.contentHeight = this.layoutEntries(this.contentWidth);
        }

        this.layoutDirty = false;
        this.lastLayoutX = this.getX();
        this.lastLayoutY = this.getY();
        this.lastLayoutWidth = this.getWidth();
        this.lastLayoutHeight = this.getHeight();
        this.lastGroupIndex = this.activeGroupIndex;
    }

    private int layoutEntries(int contentWidth) {
        int x = this.getX() + ELEMENT_SIDE_PADDING;
        int y = this.getY() - Mth.floor(this.scrollAmount);
        int totalHeight = 0;

        List<ConfigListEntry> entries = this.activeEntries();
        for (int i = 0; i < entries.size(); i++) {
            ConfigListEntry entry = entries.get(i);
            entry.updateEntryLayout(x, y, contentWidth);
            y += entry.getHeight();
            totalHeight += entry.getHeight();
            if (i < entries.size() - 1) {
                y += ELEMENT_SPACING;
                totalHeight += ELEMENT_SPACING;
            }
        }

        return totalHeight;
    }

    private void setScrollAmount(double amount) {
        double clamped = Mth.clamp(amount, 0.0D, this.getMaxScroll());
        if (clamped != this.scrollAmount) {
            this.scrollAmount = clamped;
            this.invalidateLayout();
        }
    }

    private double getMaxScroll() {
        return Math.max(0.0D, this.contentHeight - this.getHeight());
    }

    private boolean isScrollbarVisible() {
        return this.contentHeight > this.getHeight();
    }

    private int getScrollbarThumbHeight(int contentTop, int contentBottom) {
        int viewportHeight = Math.max(0, contentBottom - contentTop);
        if (viewportHeight <= 0 || this.contentHeight <= 0) {
            return viewportHeight;
        }

        int thumb = (int) ((float) viewportHeight * viewportHeight / this.contentHeight);
        int maxThumbHeight = Math.max(1, viewportHeight - 8);
        int minThumbHeight = Math.min(32, maxThumbHeight);
        return Mth.clamp(thumb, minThumbHeight, maxThumbHeight);
    }

    private int getScrollbarThumbTop(int contentTop, int contentBottom) {
        double maxScroll = this.getMaxScroll();
        if (maxScroll <= 0.0D) {
            return contentTop;
        }

        int thumbHeight = this.getScrollbarThumbHeight(contentTop, contentBottom);
        int travel = Math.max(0, (contentBottom - contentTop) - thumbHeight);
        return contentTop + Mth.floor(this.scrollAmount * travel / maxScroll);
    }

    private void scrollToMouse(double mouseY, int contentTop, int contentBottom) {
        double maxScroll = this.getMaxScroll();
        if (maxScroll <= 0.0D) {
            this.setScrollAmount(0.0D);
            return;
        }

        int thumbHeight = this.getScrollbarThumbHeight(contentTop, contentBottom);
        double trackHeight = Math.max(1.0D, (contentBottom - contentTop) - thumbHeight);
        double thumbTop = Mth.clamp(mouseY - contentTop - this.scrollbarGrabOffset, 0.0D, trackHeight);
        this.setScrollAmount(thumbTop / trackHeight * maxScroll);
    }

    private double resolveScrollbarGrabOffset(double mouseY, int contentTop, int contentBottom) {
        int thumbTop = this.getScrollbarThumbTop(contentTop, contentBottom);
        int thumbHeight = this.getScrollbarThumbHeight(contentTop, contentBottom);
        int thumbBottom = thumbTop + thumbHeight;
        if (mouseY >= thumbTop && mouseY < thumbBottom) {
            return mouseY - thumbTop;
        }
        return thumbHeight / 2.0D;
    }

    private void renderScrollbar(GuiGraphicsExtractor graphics, int right, int contentTop, int contentBottom) {
        int trackHeight = Math.max(0, contentBottom - contentTop);
        if (trackHeight <= 0 || !this.isScrollbarVisible()) {
            return;
        }

        int barLeft = right - SCROLLBAR_WIDTH;
        int thumbHeight = this.getScrollbarThumbHeight(contentTop, contentBottom);
        int thumbTop = this.getScrollbarThumbTop(contentTop, contentBottom);

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_BACKGROUND_SPRITE, barLeft, contentTop, SCROLLBAR_WIDTH, trackHeight);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE, barLeft, thumbTop, SCROLLBAR_WIDTH, thumbHeight);
    }

    private List<ConfigListEntry> activeEntries() {
        ListGroup activeGroup = this.getActiveGroup();
        return activeGroup != null ? activeGroup.entries() : List.of();
    }
}
