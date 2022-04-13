package com.eventsourcing.es;

import java.util.List;

public interface EventBus {
    void publish(List<Event> events);
}