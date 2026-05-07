package com.nine.softimprints.client.ui.component.group;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class GroupSwitcher<T> extends AbstractWidget {
	
	private final List<GroupEntry<T>> rawEntries = new ArrayList<>();
	private final List<GroupEntry<T>> entries = new ArrayList<>();
	private final Consumer<T> onOptionChanged;
	private int activeIndex = -1;
	
	public GroupSwitcher(
			int x,
			int y,
			int width,
			int height,
			Consumer<T> onOptionChanged
	) {
		super(x, y, width, height, Component.empty());
		this.onOptionChanged = onOptionChanged;
	}

	public int size (){
		return entries.size();
	}
	
	public void addGroupEntry(GroupEntry<T> entry){
		this.rawEntries.add(entry);
		List<GroupEntry<T>> updated = new ArrayList<>(this.rawEntries);
		this.entries.clear();
		this.entries.addAll(distributeWidths(updated, getWidth()));
		if (this.activeIndex >= this.entries.size()) {
			this.activeIndex = -1;
		}
	}
	
	public T getActiveValue() {
		if (this.activeIndex < 0) {
			return null;
		}
		return this.optionAt(this.activeIndex).value();
	}
	
	public void setActiveValue(T value) {
		this.activeIndex = this.indexOf(value);
	}
	
	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		int currentX = this.getX();
		for (int i = 0; i < this.entries.size(); i++) {
			GroupEntry<T> opt = this.entries.get(i);
			boolean isActive = i == this.activeIndex;
			boolean hovered = this.resolveIndex(mouseX, mouseY) == i;
			opt.render(graphics, this, currentX, getY(), getHeight(), isActive, hovered);
			currentX += opt.width();
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
		if (!this.active || !this.visible || button != 0 || !this.isMouseOver(mouseX, mouseY)) {
			return false;
		}
		
		int clickedIndex = this.resolveIndex(mouseX, mouseY);
		if (clickedIndex < 0 || clickedIndex >= this.entries.size()) {
			return false;
		}
		if (clickedIndex == this.activeIndex) {
			return true;
		}
		if (!this.entries.get(clickedIndex).interactive()) {
			return false;
		}
		
		this.activeIndex = clickedIndex;
		this.onOptionChanged.accept(this.optionAt(clickedIndex).value());
		
		playDownSound(Minecraft.getInstance().getSoundManager());
		
		return true;
	}
	
	@Override
	public boolean keyPressed(KeyEvent event) {
		if (!this.isFocused()) {
			return false;
		}
		boolean left = event.isLeft();
		boolean right = event.isRight();
		if (!left && !right) {
			return false;
		}

		int next = activeIndex < 0
				? firstInteractiveIndexForDirection(left)
				: nextInteractiveIndex(this.activeIndex, left ? -1 : 1);
		if (next < 0 || next == this.activeIndex) {
			return false;
		}

		this.activeIndex = next;
		this.onOptionChanged.accept(this.optionAt(next).value());
		playDownSound(Minecraft.getInstance().getSoundManager());
		return true;
	}

	private int nextInteractiveIndex(int from, int step) {
		int size = this.entries.size();
		for (int i = from + step; i >= 0 && i < size; i += step) {
			if (this.entries.get(i).interactive()) {
				return i;
			}
		}
		return -1;
	}

	private int firstInteractiveIndexForDirection(boolean left) {
		if (!left) {
			return findFirstInteractiveIndex(this.entries);
		}
		for (int i = this.entries.size() - 1; i >= 0; i--) {
			if (this.entries.get(i).interactive()) {
				return i;
			}
		}
		return -1;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		this.defaultButtonNarrationText(output);
	}
	
	
	private static <T> List<GroupEntry<T>> distributeWidths(List<GroupEntry<T>> input, int totalWidth) {
		int totalContentWidth = 0;
		int totalSpaceWidth = 0;
		int interactiveCount = 0;
		
		for (GroupEntry<T> entry : input) {
			if (entry.interactive()) {
				totalContentWidth += entry.width();
				interactiveCount++;
			} else {
				totalSpaceWidth += entry.width();
			}
		}
		
		int extra = totalWidth - totalContentWidth - totalSpaceWidth;
		int perInteractive = interactiveCount == 0 ? 0 : extra / interactiveCount;
		int[] trailingSpacerWidths = new int[input.size() + 1];
		for (int i = input.size() - 1; i >= 0; i--) {
			trailingSpacerWidths[i] = trailingSpacerWidths[i + 1] + (input.get(i).interactive() ? 0 : input.get(i).width());
		}
		
		List<GroupEntry<T>> result = new ArrayList<>(input.size());
		int used = 0;
		int interactiveSeen = 0;
		for (int i = 0; i < input.size(); i++) {
			GroupEntry<T> opt = input.get(i);
			int w = opt.width();
			if (opt.interactive()) {
				if (interactiveSeen == interactiveCount - 1) {
					w = totalWidth - used - trailingSpacerWidths[i + 1];
				} else {
					w = opt.width() + perInteractive;
				}
				interactiveSeen++;
			}
			result.add(opt.withWidth(w));
			used += w;
		}
		return List.copyOf(result);
	}
	
	private int indexOf(T option) {
		for (int i = 0; i < this.entries.size(); i++) {
			GroupEntry<T> entry = this.entries.get(i);
			if (entry instanceof OptionEntry<T> selectable && Objects.equals(selectable.value(), option)) {
				return i;
			}
		}
		return -1;
	}
	
	private int resolveIndex(double mouseX, double mouseY) {
		if (!this.isMouseOver(mouseX, mouseY)) {
			return -1;
		}
		
		double localX = mouseX - this.getX();
		double localY = mouseY - this.getY();
		int accumulated = 0;
		for (int i = 0; i < this.entries.size(); i++) {
			int w = this.entries.get(i).width();
			boolean yMatch = true;
			if (activeIndex != i) {
				yMatch = localY > 3;
			}
			if (localX >= accumulated && localX < accumulated + w && yMatch) {
				return i;
			}
			accumulated += w;
		}
		return -1;
	}

	private static <T> int findFirstInteractiveIndex(List<GroupEntry<T>> entries) {
		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).interactive()) {
				return i;
			}
		}
		return -1;
	}

	private OptionEntry<T> optionAt(int index) {
		GroupEntry<T> entry = this.entries.get(index);
		if (entry instanceof OptionEntry<T> option) {
			return option;
		}
		throw new IllegalStateException("Active entry must be selectable");
	}
	
	
}
