package me.nyxion.module.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ListSetting<T> extends Setting<List<T>> {
    private final int maxSize;
    private final Predicate<T> elementValidator;
    
    public ListSetting(String name, String description, List<T> defaultValue, int maxSize) {
        super(name, description, new ArrayList<>(defaultValue));
        this.maxSize = maxSize;
        this.elementValidator = null;
    }
    
    public ListSetting(String name, String description, List<T> defaultValue, int maxSize, Predicate<T> elementValidator, Consumer<List<T>> onChange) {
        super(name, description, new ArrayList<>(defaultValue), list -> list.size() <= maxSize, onChange);
        this.maxSize = maxSize;
        this.elementValidator = elementValidator;
    }
    
    public boolean addElement(T element) {
        if (getValue().size() >= maxSize || (elementValidator != null && !elementValidator.test(element))) {
            return false;
        }
        List<T> newList = new ArrayList<>(getValue());
        newList.add(element);
        setValue(newList);
        return true;
    }
    
    public boolean removeElement(T element) {
        List<T> newList = new ArrayList<>(getValue());
        boolean removed = newList.remove(element);
        if (removed) {
            setValue(newList);
        }
        return removed;
    }
    
    public int getMaxSize() {
        return maxSize;
    }
}