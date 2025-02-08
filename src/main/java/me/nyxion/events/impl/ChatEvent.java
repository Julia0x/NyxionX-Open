package me.nyxion.events.impl;

import me.nyxion.events.Event;
import me.nyxion.events.EventType;

public class ChatEvent extends Event {
    private String message;
    
    public ChatEvent(String message) {
        super(EventType.PRE);
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}