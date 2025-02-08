package me.nyxion.module.settings;

import java.awt.Color;
import java.util.function.Consumer;

public class ColorSetting extends Setting<Color> {
    public ColorSetting(String name, String description, Color defaultValue) {
        super(name, description, defaultValue);
    }
    
    public ColorSetting(String name, String description, Color defaultValue, Consumer<Color> onChange) {
        super(name, description, defaultValue, null, onChange);
    }
    
    public int getRed() {
        return getValue().getRed();
    }
    
    public int getGreen() {
        return getValue().getGreen();
    }
    
    public int getBlue() {
        return getValue().getBlue();
    }
    
    public int getAlpha() {
        return getValue().getAlpha();
    }
    
    public void setRed(int red) {
        setValue(new Color(red, getGreen(), getBlue(), getAlpha()));
    }
    
    public void setGreen(int green) {
        setValue(new Color(getRed(), green, getBlue(), getAlpha()));
    }
    
    public void setBlue(int blue) {
        setValue(new Color(getRed(), getGreen(), blue, getAlpha()));
    }
    
    public void setAlpha(int alpha) {
        setValue(new Color(getRed(), getGreen(), getBlue(), alpha));
    }
}