package com.nine.softimprints.ui.layout;

import net.minecraft.client.gui.components.AbstractWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class LinearLayout implements UILayoutElement {

    private final Axis axis;
    private final List<Slot> slots = new ArrayList<>();
    private LayoutRect bounds;

    private LinearLayout(
            Axis axis,
            LayoutRect bounds
    ) {
        this.axis = axis;
        this.bounds = Objects.requireNonNull(bounds, "bounds");
    }

    public static LinearLayout row(LayoutRect bounds) {
        return new LinearLayout(Axis.HORIZONTAL, bounds);
    }

    public static LinearLayout column(LayoutRect bounds) {
        return new LinearLayout(Axis.VERTICAL, bounds);
    }

    public LinearLayout bounds(LayoutRect bounds) {
        this.bounds = Objects.requireNonNull(bounds, "bounds");
        return this;
    }

    public LinearLayout fixed(
            int size,
            AbstractWidget widget
    ) {
        return fixed(size, new WidgetElement(widget));
    }

    public LinearLayout fixed(
            int size,
            UILayoutElement element
    ) {
        slots.add(Slot.fixed(Math.max(0, size), element));
        return this;
    }

    public LinearLayout weight(
            int weight,
            AbstractWidget widget
    ) {
        return weight(weight, new WidgetElement(widget));
    }

    public LinearLayout weight(
            int weight,
            UILayoutElement element
    ) {
        slots.add(Slot.weight(Math.max(0, weight), element));
        return this;
    }

    public LinearLayout gap(int size) {
        return fixed(size, EmptyElement.INSTANCE);
    }

    public LinearLayout spacer(int weight) {
        return weight(weight, EmptyElement.INSTANCE);
    }

    public void apply(Consumer<AbstractWidget> add) {
        place(bounds);
        addWidgets(add);
    }

    @Override
    public void place(LayoutRect bounds) {
        this.bounds = bounds;

        int totalFixed = 0;
        int totalWeight = 0;
        for (Slot slot : slots) {
            if (slot.fixed()) {
                totalFixed += slot.size();
            } else {
                totalWeight += slot.weight();
            }
        }

        int available = Math.max(0, axis.length(bounds) - totalFixed);
        int cursor = axis.start(bounds);
        int remaining = available;
        int remainingWeight = totalWeight;

        for (Slot slot : slots) {
            int size;
            if (slot.fixed()) {
                size = slot.size();
            } else if (remainingWeight <= 0) {
                size = 0;
            } else {
                size = remaining * slot.weight() / remainingWeight;
                remaining -= size;
                remainingWeight -= slot.weight();
            }

            slot.element().place(axis.slice(bounds, cursor, size));
            cursor += size;
        }
    }

    @Override
    public void addWidgets(Consumer<AbstractWidget> add) {
        for (Slot slot : slots) {
            slot.element().addWidgets(add);
        }
    }

    private enum Axis {
        HORIZONTAL {
            @Override
            int start(LayoutRect bounds) {
                return bounds.x();
            }

            @Override
            int length(LayoutRect bounds) {
                return bounds.width();
            }

            @Override
            LayoutRect slice(
                    LayoutRect bounds,
                    int start,
                    int length
            ) {
                return new LayoutRect(start, bounds.y(), length, bounds.height());
            }
        },
        VERTICAL {
            @Override
            int start(LayoutRect bounds) {
                return bounds.y();
            }

            @Override
            int length(LayoutRect bounds) {
                return bounds.height();
            }

            @Override
            LayoutRect slice(
                    LayoutRect bounds,
                    int start,
                    int length
            ) {
                return new LayoutRect(bounds.x(), start, bounds.width(), length);
            }
        };

        abstract int start(LayoutRect bounds);

        abstract int length(LayoutRect bounds);

        abstract LayoutRect slice(
                LayoutRect bounds,
                int start,
                int length
        );
    }

    private record Slot(int size, int weight, UILayoutElement element) {

        private Slot {
            Objects.requireNonNull(element, "element");
        }

        static Slot fixed(
                int size,
                UILayoutElement element
        ) {
            return new Slot(size, 0, element);
        }

        static Slot weight(
                int weight,
                UILayoutElement element
        ) {
            return new Slot(0, weight, element);
        }

        boolean fixed() {
            return weight == 0;
        }
    }
}
