package com.nine.softimprints.ui.component.slider;

import com.nine.softimprints.ui.util.KeyMods;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

// Neoforge ExtendedSlider.class copy
public class ExtendedSlider extends AbstractSliderButton {

    private final DecimalFormat format;
    private final List<Consumer<Double>> listeners = new ArrayList<>();
    protected String labelKey;
    protected double minValue;
    protected double maxValue;
    protected double stepSize;
    protected boolean drawString;
    protected int precision;
    private Function<Double, Component> valueFormatter;
    private Function<Double, Component> tooltipFormatter;
    private boolean customValueFormatter;

    public ExtendedSlider(
            int x,
            int y,
            int width,
            int height,
            String labelKey,
            double minValue,
            double maxValue,
            double currentValue,
            double stepSize,
            int precision,
            boolean drawString
    ) {
        super(x, y, width, height, Component.empty(), 0.0D);

        this.labelKey = labelKey;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.stepSize = Math.abs(stepSize);
        this.drawString = drawString;
        this.precision = precision;
        this.value = this.snapToNearest(this.fractionFor(currentValue));

        if (stepSize == 0.0D) {
            int displayPrecision = Math.min(precision, 4);

            StringBuilder builder = new StringBuilder("0");
            if (displayPrecision > 0) {
                builder.append('.');
            }
            while (displayPrecision-- > 0) {
                builder.append('0');
            }

            this.format = new DecimalFormat(builder.toString());
        } else if (Mth.equal(this.stepSize, Math.floor(this.stepSize))) {
            this.format = new DecimalFormat("0");
        } else {
            this.format = new DecimalFormat(Double.toString(this.stepSize).replaceAll("\\d", "0"));
        }
        this.valueFormatter = this::defaultValueComponent;

        this.updateMessage();
    }

    public ExtendedSlider(
            int x,
            int y,
            int width,
            int height,
            String labelKey,
            double minValue,
            double maxValue,
            double currentValue,
            boolean drawString
    ) {
        this(x, y, width, height, labelKey, minValue, maxValue, currentValue, 1.0D, 0, drawString);
    }

    public static Builder builder(String labelKey) {
        return new Builder(labelKey);
    }

    public ExtendedSlider addListener(Consumer<Double> listener) {
        listeners.add(listener);
        return this;
    }

    public double getValue() {
        return this.value * (this.maxValue - this.minValue) + this.minValue;
    }

    public void setValue(double value) {
        this.setFractionalValue(this.fractionFor(value));
    }

    public long getValueLong() {
        return Math.round(this.getValue());
    }

    public int getValueInt() {
        return (int) this.getValueLong();
    }

    public String getValueString() {
        return this.getValueComponent().getString();
    }

    public Component getValueComponent() {
        Component ret = this.valueFormatter.apply(this.getValue());
        return ret != null ? ret : Component.empty();
    }

    public ExtendedSlider setValueFormatter(Function<Double, Component> valueFormatter) {
        this.customValueFormatter = valueFormatter != null;
        this.valueFormatter = this.customValueFormatter ? valueFormatter : this::defaultValueComponent;
        this.updateMessage();
        return this;
    }

    public ExtendedSlider setTooltipFormatter(Function<Double, Component> tooltipFormatter) {
        this.tooltipFormatter = tooltipFormatter;
        this.updateDynamicTooltip();
        return this;
    }

    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
        this.updateMessage();
    }

    public void setDrawString(boolean drawString) {
        this.drawString = drawString;
        this.updateMessage();
    }

    @Override
    public boolean mouseScrolled(
            double x,
            double y,
            double scrollX,
            double scrollY
    ) {
        if (isMouseOver(x, y) && (KeyMods.shiftPressed() || KeyMods.altPressed())) {
            this.setValue(this.getValue() + this.stepSize * (scrollY > 0 ? 1 : -1));
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public void onClick(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        this.setValueFromMouse(event.x());
    }

    @Override
    protected void onDrag(
            MouseButtonEvent event,
            double dragX,
            double dragY
    ) {
        this.setValueFromMouse(event.x());
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        boolean moveLeft = keyEvent.isLeft();
        boolean moveRight = keyEvent.isRight();

        if (!moveLeft && !moveRight) {
            return super.keyPressed(keyEvent);
        }

        boolean decrease = moveLeft;
        if (this.minValue > this.maxValue) {
            decrease = !decrease;
        }

        double direction = decrease ? -1.0D : 1.0D;
        if (this.stepSize <= 0.0D) {
            this.setFractionalValue(this.value + (direction / (this.width - 8)));
        } else {
            this.setValue(this.getValue() + direction * this.stepSize);
        }

        return true;
    }

    @Override
    public void updateMessage() {
        if (this.drawString && this.labelKey != null) {
            this.setMessage(Component.translatable(this.labelKey, this.getValueComponent()));
        } else {
            this.setMessage(Component.empty());
        }
    }

    @Override
    protected void applyValue() {
        var v = this.getValue();
        this.listeners.forEach(l -> l.accept(v));
        this.updateMessage();
        this.updateDynamicTooltip();
    }

    private void updateDynamicTooltip() {
        if (this.tooltipFormatter == null) {
            return;
        }
        Component tooltip = this.tooltipFormatter.apply(this.getValue());
        this.setTooltip(tooltip == null ? null : Tooltip.create(tooltip));
    }

    private void setValueFromMouse(double mouseX) {
        this.setFractionalValue((mouseX - (this.getX() + 4)) / (this.width - 8));
    }

    private void setFractionalValue(double fractionalValue) {
        double oldValue = this.value;
        this.value = this.snapToNearest(fractionalValue);
        if (!Mth.equal(oldValue, this.value)) {
            this.applyValue();
        }
    }

    private double fractionFor(double value) {
        if (Mth.equal(this.minValue, this.maxValue)) {
            return 0.0D;
        }
        return (value - this.minValue) / (this.maxValue - this.minValue);
    }

    private double snapToNearest(double value) {
        if (Mth.equal(this.minValue, this.maxValue)) {
            return 0.0D;
        }
        if (this.stepSize <= 0.0D) {
            return Mth.clamp(value, 0.0D, 1.0D);
        }

        value = Mth.lerp(Mth.clamp(value, 0.0D, 1.0D), this.minValue, this.maxValue);
        value = this.minValue + this.stepSize * Math.round((value - this.minValue) / this.stepSize);

        if (this.minValue > this.maxValue) {
            value = Mth.clamp(value, this.maxValue, this.minValue);
        } else {
            value = Mth.clamp(value, this.minValue, this.maxValue);
        }

        return Mth.map(value, this.minValue, this.maxValue, 0.0D, 1.0D);
    }

    private Component defaultValueComponent(double value) {
        return Component.literal(this.format.format(value));
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {

        private final String labelKey;
        private int x;
        private int y;
        private int width = 1;
        private int height = 20;
        private double minValue;
        private double maxValue = 1.0D;
        private double currentValue;
        private double stepSize = 1.0D;
        private int precision;
        private boolean drawString = true;
        private Function<Double, Component> valueFormatter;
        private List<Consumer<Double>> listeners = new ArrayList<>();

        public Builder(ExtendedSlider slider) {
            this.x = slider.getX();
            this.y = slider.getY();
            this.width = slider.width;
            this.height = slider.height;
            this.labelKey = slider.labelKey;
            this.minValue = slider.minValue;
            this.maxValue = slider.maxValue;
            this.currentValue = slider.getValue();
            this.stepSize = slider.stepSize;
            this.precision = slider.precision;
            this.drawString = slider.drawString;
            this.valueFormatter = slider.customValueFormatter ? slider.valueFormatter : null;
            this.listeners = slider.listeners;
        }

        private Builder(String labelKey) {
            this.labelKey = labelKey;
        }

        public Builder bounds(
                int x,
                int y,
                int width,
                int height
        ) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder range(
                double minValue,
                double maxValue
        ) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            return this;
        }

        public Builder value(double currentValue) {
            this.currentValue = currentValue;
            return this;
        }

        public Builder step(
                double stepSize,
                int precision
        ) {
            this.stepSize = stepSize;
            this.precision = precision;
            return this;
        }

        public Builder drawString(boolean drawString) {
            this.drawString = drawString;
            return this;
        }

        public Builder valueFormatter(Function<Double, Component> valueFormatter) {
            this.valueFormatter = valueFormatter;
            return this;
        }

        public ExtendedSlider build() {
            ExtendedSlider slider = new ExtendedSlider(
                    x, y, width, height,
                    labelKey,
                    minValue, maxValue, currentValue,
                    stepSize, precision,
                    drawString
            );
            if (valueFormatter != null) {
                slider.setValueFormatter(valueFormatter);
            }
            this.listeners.forEach(slider::addListener);
            return slider;
        }
    }
}
