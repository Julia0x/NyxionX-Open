package me.nyxion.events.impl;

import me.nyxion.events.Event;
import me.nyxion.events.EventType;

public class RenderEvent extends Event {
    private final float partialTicks;
    private final boolean is3D;
    
    public RenderEvent(float partialTicks, boolean is3D) {
        super(EventType.PRE);
        this.partialTicks = partialTicks;
        this.is3D = is3D;
    }
    
    public float getPartialTicks() {
        return partialTicks;
    }
    
    public boolean is3D() {
        return is3D;
    }
}