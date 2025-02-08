package me.nyxion.module.settings;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;
    private Consumer<String> onChange;

    public ModeSetting(String name, String description, String defaultValue, String... modes) {
        super(name, description, defaultValue, value -> Arrays.asList(modes).contains(value), null);
        this.modes = Arrays.asList(modes);
    }

    public List<String> getModes() {
        return modes;
    }

    public void cycle() {
        int index = modes.indexOf(getValue());
        String newValue = modes.get((index + 1) % modes.size());
        setValue(newValue);
        if (onChange != null) {
            onChange.accept(newValue);
        }
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        if (onChange != null) {
            onChange.accept(value);
        }
    }

    public void setOnChange(Consumer<String> onChange) {
        this.onChange = onChange;
    }
}