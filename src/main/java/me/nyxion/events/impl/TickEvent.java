package me.nyxion.events.impl;

import me.nyxion.events.Event;
import me.nyxion.events.EventType;

public class TickEvent extends Event {
    public TickEvent() {
        super(EventType.PRE);
    }
}