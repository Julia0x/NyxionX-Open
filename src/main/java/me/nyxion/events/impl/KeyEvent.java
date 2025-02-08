package me.nyxion.events.impl;

import me.nyxion.events.Event;
import me.nyxion.events.EventType;

public class KeyEvent extends Event {
    private final int keyCode;
    
    public KeyEvent(int keyCode) {
        super(EventType.PRE);
        this.keyCode = keyCode;
    }
    
    public int getKeyCode() {
        return keyCode;
    }
}