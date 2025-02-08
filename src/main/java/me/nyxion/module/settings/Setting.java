package me.nyxion.module.settings;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class Setting<T> {
    private final String name;
    private final String description;
    private T value;
    private final Predicate<T> validator;
    private final Consumer<T> onChange;
    
    public Setting(String name, String description, T defaultValue) {
        this(name, description, defaultValue, null, null);
    }
    
    public Setting(String name, String description, T defaultValue, Predicate<T> validator, Consumer<T> onChange) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
        this.validator = validator;
        this.onChange = onChange;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public T getValue() {
        return value;
    }
    
    public void setValue(T value) {
        if (validator == null || validator.test(value)) {
            this.value = value;
            if (onChange != null) {
                onChange.accept(value);
            }
        }
    }
}