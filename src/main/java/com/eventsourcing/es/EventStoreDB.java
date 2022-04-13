package com.eventsourcing.es;

import java.util.List;

public interface EventStoreDB {

    void saveEvents(final List<Event> events);

    List<Event> loadEvents(final String aggregateId, long version);

    <T extends AggregateRoot> void save(final T aggregate);

    <T extends AggregateRoot> T load(final String aggregateId, final Class<T> aggregateType);

    Boolean exists(final String aggregateId);
}
