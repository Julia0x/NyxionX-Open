package me.nyxion.module.settings;

import java.util.function.Consumer;

public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double increment;
    
    public NumberSetting(String name, String description, double defaultValue, double min, double max, double increment) {
        super(name, description, defaultValue, value -> value >= min && value <= max, null);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }
    
    public NumberSetting(String name, String description, double defaultValue, double min, double max, double increment, Consumer<Double> onChange) {
        super(name, description, defaultValue, value -> value >= min && value <= max, onChange);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }
    
    public double getMin() {
        return min;
    }
    
    public double getMax() {
        return max;
    }
    
    public double getIncrement() {
        return increment;
    }
}