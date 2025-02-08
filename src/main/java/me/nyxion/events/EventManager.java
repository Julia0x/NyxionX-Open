package me.nyxion.events;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager {
    private final Map<Class<? extends Event>, List<EventData>> registeredListeners = new ConcurrentHashMap<>();
    private final Queue<Event> eventQueue = new ArrayDeque<>();
    private boolean processingEvents = false;

    public void init() {
        System.out.println("[Nyxion] Event system initialized!");
    }

    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventHandler.class) && method.getParameterCount() == 1) {
                Class<?> eventClass = method.getParameterTypes()[0];
                if (Event.class.isAssignableFrom(eventClass)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> castedEventClass = (Class<? extends Event>) eventClass;
                    
                    EventData data = new EventData(listener, method, method.getAnnotation(EventHandler.class).priority());
                    registeredListeners.computeIfAbsent(castedEventClass, k -> new CopyOnWriteArrayList<>()).add(data);
                    
                    // Sort by priority
                    registeredListeners.get(castedEventClass).sort((a, b) -> 
                        Integer.compare(b.priority.getValue(), a.priority.getValue()));
                }
            }
        }
    }

    public void unregister(Object listener) {
        registeredListeners.values().forEach(list -> 
            list.removeIf(data -> data.instance == listener));
    }

    public void call(Event event) {
        if (processingEvents) {
            eventQueue.offer(event);
            return;
        }

        processEvent(event);
        processEventQueue();
    }

    private void processEvent(Event event) {
        List<EventData> listeners = registeredListeners.get(event.getClass());
        if (listeners != null) {
            processingEvents = true;
            try {
                for (EventData data : listeners) {
                    if (!event.isCancelled() || data.priority == EventPriority.HIGHEST) {
                        try {
                            data.method.invoke(data.instance, event);
                        } catch (Exception e) {
                            System.err.println("[Nyxion] Error handling event: " + event.getClass().getSimpleName());
                            e.printStackTrace();
                        }
                    }
                }
            } finally {
                processingEvents = false;
            }
        }
    }

    private void processEventQueue() {
        Event event;
        while ((event = eventQueue.poll()) != null) {
            processEvent(event);
        }
    }

    private static class EventData {
        private final Object instance;
        private final Method method;
        private final EventPriority priority;

        public EventData(Object instance, Method method, EventPriority priority) {
            this.instance = instance;
            this.method = method;
            this.priority = priority;
            this.method.setAccessible(true);
        }
    }
}