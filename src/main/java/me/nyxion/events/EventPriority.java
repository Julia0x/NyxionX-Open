package me.nyxion.events;

public enum EventPriority {
    HIGHEST(2),
    HIGH(1),
    MEDIUM(0),
    LOW(-1),
    LOWEST(-2);

    private final int value;

    EventPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}