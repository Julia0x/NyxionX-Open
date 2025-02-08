package me.nyxion.module.settings;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class StringSetting extends Setting<String> {
    private final int maxLength;
    private Consumer<String> onChange;

    public StringSetting(String name, String description, String defaultValue, int maxLength) {
        super(name, description, defaultValue, value -> value.length() <= maxLength, null);
        this.maxLength = maxLength;
    }

    public StringSetting(String name, String description, String defaultValue, int maxLength, Predicate<String> validator, Consumer<String> onChange) {
        super(name, description, defaultValue, value -> value.length() <= maxLength && validator.test(value), onChange);
        this.maxLength = maxLength;
        this.onChange = onChange;
    }

    public void setOnChange(Consumer<String> onChange) {
        this.onChange = onChange;
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        if (onChange != null) {
            onChange.accept(value);
        }
    }

    public int getMaxLength() {
        return maxLength;
    }
}