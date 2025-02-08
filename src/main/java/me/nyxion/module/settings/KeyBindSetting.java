package me.nyxion.module.settings;

import org.lwjgl.input.Keyboard;
import java.util.function.Consumer;

public class KeyBindSetting extends Setting<Integer> {
    public KeyBindSetting(String name, String description, int defaultKey) {
        super(name, description, defaultKey);
    }
    
    public KeyBindSetting(String name, String description, int defaultKey, Consumer<Integer> onChange) {
        super(name, description, defaultKey, null, onChange);
    }
    
    public String getKeyName() {
        return getValue() == Keyboard.KEY_NONE ? "None" : Keyboard.getKeyName(getValue());
    }
}