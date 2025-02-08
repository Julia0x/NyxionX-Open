package me.nyxion.events.impl;

import me.nyxion.events.Event;
import me.nyxion.events.EventType;
import net.minecraft.network.Packet;

public class PacketEvent extends Event {
    private final Packet<?> packet;
    private final Direction direction;
    
    public PacketEvent(Packet<?> packet, Direction direction) {
        super(EventType.PRE);
        this.packet = packet;
        this.direction = direction;
    }
    
    public Packet<?> getPacket() {
        return packet;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public enum Direction {
        INCOMING,
        OUTGOING
    }
}