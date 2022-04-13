package com.eventsourcing.es;

public interface Projection {
   void when(Event event);
}