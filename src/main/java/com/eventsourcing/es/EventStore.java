package com.eventsourcing.es;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EventStore implements EventStoreDB {

    //    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void saveEvents(List<Event> events) {
        if (events.isEmpty()) return;

        final List<Event> changes = new ArrayList<>(events);
        changes.forEach(event -> {
            int result = jdbcTemplate.update("INSERT INTO events (aggregate_id, aggregate_type, event_type, data, metadata, version, timestamp) " +
                            "values (:aggregate_id, :aggregate_type, :event_type, :data, :metadata, :version, now())",
                    Map.of("aggregate_id", event.getAggregateId(),
                            "aggregate_type", event.getAggregateType(),
                            "event_type", event.getEventType(),
                            "data", Objects.isNull(event.getData()) ? new byte[]{} : event.getData(),
                            "metadata", Objects.isNull(event.getMetaData()) ? new byte[]{} : event.getMetaData(),
                            "version", event.getVersion()));

            log.info("(saveEvents) saved event: {}", event);
        });
    }

    private Snapshot getSnapshot(String aggregateId) {

        return new Snapshot();
    }

    @Override
    public List<Event> loadEvents(String aggregateId, long version) {
        final List<Event> events = jdbcTemplate.query("select event_id ,aggregate_id, aggregate_type, event_type, data, metadata, version, timestamp" +
                        " from events e where e.aggregate_id = :aggregate_id and e.version > :version ORDER BY e.version ASC",
                Map.of("aggregate_id", aggregateId, "version", version),
                (rs, rowNum) -> {
                    Event event = Event.builder()
                            .aggregateId(rs.getString("aggregate_id"))
                            .aggregateType(rs.getString("aggregate_type"))
                            .eventType(rs.getString("event_type"))
                            .data(rs.getBytes("data"))
                            .metaData(rs.getBytes("metadata"))
                            .version(rs.getLong("version"))
                            .timeStamp(rs.getTimestamp("timestamp").toLocalDateTime())
                            .build();
                    return event;
                });

        log.info("(loadEvents) events list: {}", events);
        return events;
    }

    private <T extends AggregateRoot> void saveSnapshot(T aggregate) {
        aggregate.toSnapshot();
        final var snapshot = EventSourcingUtils.snapshotFromAggregate(aggregate);

        int update = jdbcTemplate.update("INSERT INTO snapshots (aggregate_id, aggregate_type, data, metadata, version, timestamp) " +
                        "VALUES (:aggregate_id, :aggregate_type, :data, :metadata, :version, now()) " +
                        "ON CONFLICT (aggregate_id) " +
                        "DO UPDATE SET data = :data, version = :version, timestamp = now()",
                Map.of("aggregate_id", snapshot.getAggregateId(),
                        "aggregate_type", snapshot.getAggregateType(),
                        "data", Objects.isNull(snapshot.getData()) ? new byte[]{} : snapshot.getData(),
                        "metadata", Objects.isNull(snapshot.getMetaData()) ? new byte[]{} : snapshot.getMetaData(),
                        "version", snapshot.getVersion()));
        log.info("(saveSnapshot) result: {}", update);
    }

    @Override
    @Transactional
    public <T extends AggregateRoot> void save(T aggregate) {
        if (aggregate.getVersion() > 1) {
            this.handleConcurrency(aggregate.getId());
        }

        this.saveEvents(aggregate.getChanges());
        if (aggregate.getVersion() % 3 == 0) {
            this.saveSnapshot(aggregate);
        }
        log.info("(save) aggregate saved: {}", aggregate);
    }

    private void handleConcurrency(String aggregateId) {
        try {
            String aggregateID = jdbcTemplate.queryForObject("SELECT aggregate_id FROM events e " +
                    "WHERE e.aggregate_id = :aggregate_id LIMIT 1 FOR UPDATE", Map.of("aggregate_id", aggregateId), String.class);
            log.info("(handleConcurrency) aggregateID for lock: {}", aggregateID);
        } catch (EmptyResultDataAccessException e) {
            log.info("(handleConcurrency) EmptyResultDataAccessException: {}", e.getMessage());
        }
        log.info("(handleConcurrency) aggregateID for lock: {}", aggregateId);
    }

    private Optional<Snapshot> loadSnapshot(String aggregateId) {
        final Optional<Snapshot> snapshot = jdbcTemplate.query("select aggregate_id, aggregate_type, data, metadata, version, timestamp from snapshots s " +
                        "where s.aggregate_id = :aggregate_id",
                Map.of("aggregate_id", aggregateId), (rs, rowNum) -> Snapshot.builder()
                        .aggregateId(rs.getString("aggregate_id"))
                        .aggregateType(rs.getString("aggregate_type"))
                        .data(rs.getBytes("data"))
                        .metaData(rs.getBytes("metadata"))
                        .version(rs.getLong("version"))
                        .timeStamp(rs.getTimestamp("timestamp").toLocalDateTime())
                        .build()).stream().findFirst();
        snapshot.ifPresent(result -> log.info("(loadSnapshot) snapshot: {}", result));
        return snapshot;
    }

    private <T extends AggregateRoot> T getAggregate(final String aggregateId, final Class<T> aggregateType) {
        try {
            return aggregateType.getConstructor(String.class).newInstance(aggregateId);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends AggregateRoot> T getSnapshotFromClass(Optional<Snapshot> snapshot, String aggregateId, Class<T> aggregateType) {
        if (snapshot.isEmpty()) {
            final var defaultSnapshot = EventSourcingUtils.snapshotFromAggregate(getAggregate(aggregateId, aggregateType));
            return EventSourcingUtils.aggregateFromSnapshot(defaultSnapshot, aggregateType);
        }
        return EventSourcingUtils.aggregateFromSnapshot(snapshot.get(), aggregateType);
    }

    @Override
    @Transactional(readOnly = true)
    public <T extends AggregateRoot> T load(String aggregateId, Class<T> aggregateType) {

        final Optional<Snapshot> snapshot = this.loadSnapshot(aggregateId);

        final var aggregate = this.getSnapshotFromClass(snapshot, aggregateId, aggregateType);
        log.info("(load) aggregate: {}", aggregate);

        final List<Event> events = this.loadEvents(aggregateId, aggregate.getVersion());
        events.forEach(event -> {
            aggregate.raiseEvent(event);
            log.info("raise event version: {}", event.getVersion());
        });

        if (aggregate.getVersion() == 0) throw new RuntimeException("aggregate not found id:" + aggregateId);

        return aggregate;
    }

    @Override
    public Boolean exists(String aggregateId) {
        return null;
    }
}
