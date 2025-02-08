package me.nyxion.module.settings;

import java.util.function.Consumer;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }
    
    public BooleanSetting(String name, String description, boolean defaultValue, Consumer<Boolean> onChange) {
        super(name, description, defaultValue, null, onChange);
    }
    
    public void toggle() {
        setValue(!getValue());
    }
}