package me.nyxion.events;

public class Event {
    private boolean cancelled;
    private EventType type;
    
    public Event(EventType type) {
        this.type = type;
        this.cancelled = false;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public EventType getType() {
        return type;
    }
}