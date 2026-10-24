package net.xlea99.lifeguard.client.menu;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.xlea99.lifeguard.core.Settings;
import net.xlea99.lifeguard.core.SubValue;

/** A slider over a whole-number or decimal sub-value's range, editing the draft. */
final class ValueSlider extends AbstractSliderButton {
    private final SubValue<?> subValue;
    private final double min;
    private final double max;
    private final Settings draft;
    private final Runnable onChange;

    ValueSlider(int x, int y, int width, SubValue<?> subValue, Settings draft, Runnable onChange) {
        super(x, y, width, 20, Component.empty(), 0);
        this.subValue = subValue;
        this.draft = draft;
        this.onChange = onChange;
        if (subValue instanceof SubValue.Int number) {
            min = number.min();
            max = number.max();
        } else if (subValue instanceof SubValue.Decimal number) {
            min = number.min();
            max = number.max();
        } else {
            throw new IllegalArgumentException(subValue.name() + " isn't a number");
        }
        sync();
    }

    /** Moves the handle to the draft's value, e.g. after a reset. */
    void sync() {
        double current = ((Number) draft.value(subValue)).doubleValue();
        value = max == min ? 0 : (current - min) / (max - min);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        Object current = draft.value(subValue);
        setMessage(Component.literal(current instanceof Double decimal ? String.format("%.2f", decimal) : String.valueOf(current)));
    }

    @Override
    protected void applyValue() {
        double raw = min + value * (max - min);
        switch (subValue) {
            case SubValue.Int number -> draft.setValue(number, (int) Math.round(raw));
            case SubValue.Decimal number -> draft.setValue(number, Math.max(min, Math.min(max, Math.round(raw * 100) / 100.0)));
            case SubValue.Bool ignored -> throw new IllegalStateException("not a number");
        }
        onChange.run();
    }
}
