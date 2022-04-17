package com.eventsourcing.es;


import com.eventsourcing.es.exceptions.AggregateNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.eventsourcing.es.Constants.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EventStore implements EventStoreDB {

    public static final int SNAPSHOT_FREQUENCY = 3;
    private static final String SAVE_EVENTS_QUERY = "INSERT INTO events (aggregate_id, aggregate_type, event_type, data, metadata, version, timestamp) values (:aggregate_id, :aggregate_type, :event_type, :data, :metadata, :version, now())";
    private static final String LOAD_EVENTS_QUERY = "SELECT event_id ,aggregate_id, aggregate_type, event_type, data, metadata, version, timestamp FROM events e WHERE e.aggregate_id = :aggregate_id AND e.version > :version ORDER BY e.version ASC";
    private static final String SAVE_SNAPSHOT_QUERY = "INSERT INTO snapshots (aggregate_id, aggregate_type, data, metadata, version, timestamp) VALUES (:aggregate_id, :aggregate_type, :data, :metadata, :version, now()) ON CONFLICT (aggregate_id) DO UPDATE SET data = :data, version = :version, timestamp = now()";
    private static final String HANDLE_CONCURRENCY_QUERY = "SELECT aggregate_id FROM events e WHERE e.aggregate_id = :aggregate_id LIMIT 1 FOR UPDATE";
    private static final String LOAD_SNAPSHOT_QUERY = "SELECT aggregate_id, aggregate_type, data, metadata, version, timestamp FROM snapshots s WHERE s.aggregate_id = :aggregate_id";
    private static final String EXISTS_QUERY = "SELECT aggregate_id FROM events WHERE e e.aggregate_id = :aggregate_id";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final EventBus eventBus;

    @Override
    @NewSpan
    public void saveEvents(@SpanTag("events") List<Event> events) {
        if (events.isEmpty()) return;

        final List<Event> changes = new ArrayList<>(events);
        changes.forEach(event -> {
            int result = jdbcTemplate.update(SAVE_EVENTS_QUERY, Map.of(
                    AGGREGATE_ID, event.getAggregateId(),
                    AGGREGATE_TYPE, event.getAggregateType(),
                    EVENT_TYPE, event.getEventType(),
                    DATA, Objects.isNull(event.getData()) ? new byte[]{} : event.getData(),
                    METADATA, Objects.isNull(event.getMetaData()) ? new byte[]{} : event.getMetaData(),
                    VERSION, event.getVersion()));

            log.info("(saveEvents) saved result: {}, event: {}", result, event);
        });
    }

    @Override
    @NewSpan
    public List<Event> loadEvents(@SpanTag("aggregateId") String aggregateId, @SpanTag("version") long version) {
        return jdbcTemplate.query(LOAD_EVENTS_QUERY, Map.of(AGGREGATE_ID, aggregateId, VERSION, version),
                (rs, rowNum) -> Event.builder()
                        .aggregateId(rs.getString(AGGREGATE_ID))
                        .aggregateType(rs.getString(AGGREGATE_TYPE))
                        .eventType(rs.getString(EVENT_TYPE))
                        .data(rs.getBytes(DATA))
                        .metaData(rs.getBytes(METADATA))
                        .version(rs.getLong(VERSION))
                        .timeStamp(rs.getTimestamp(TIMESTAMP).toLocalDateTime())
                        .build());
    }

    @NewSpan
    private <T extends AggregateRoot> void saveSnapshot(@SpanTag("aggregate") T aggregate) {
        aggregate.toSnapshot();
        final var snapshot = EventSourcingUtils.snapshotFromAggregate(aggregate);

        int updateResult = jdbcTemplate.update(SAVE_SNAPSHOT_QUERY,
                Map.of(AGGREGATE_ID, snapshot.getAggregateId(),
                        AGGREGATE_TYPE, snapshot.getAggregateType(),
                        DATA, Objects.isNull(snapshot.getData()) ? new byte[]{} : snapshot.getData(),
                        METADATA, Objects.isNull(snapshot.getMetaData()) ? new byte[]{} : snapshot.getMetaData(),
                        VERSION, snapshot.getVersion()));

        log.info("(saveSnapshot) updateResult: {}", updateResult);
    }

    @Override
    @Transactional
    @NewSpan
    public <T extends AggregateRoot> void save(@SpanTag("aggregate") T aggregate) {
        final List<Event> aggregateEvents = new ArrayList<>(aggregate.getChanges());

        if (aggregate.getVersion() > 1) {
            this.handleConcurrency(aggregate.getId());
        }

        this.saveEvents(aggregate.getChanges());
        if (aggregate.getVersion() % SNAPSHOT_FREQUENCY == 0) {
            this.saveSnapshot(aggregate);
        }

        eventBus.publish(aggregateEvents);

        log.info("(save) saved aggregate: {}", aggregate);
    }

    @NewSpan
    private void handleConcurrency(@SpanTag("aggregateId") String aggregateId) {
        try {
            String aggregateID = jdbcTemplate.queryForObject(HANDLE_CONCURRENCY_QUERY, Map.of(AGGREGATE_ID, aggregateId), String.class);
            log.info("(handleConcurrency) aggregateID for lock: {}", aggregateID);
        } catch (EmptyResultDataAccessException e) {
            log.info("(handleConcurrency) EmptyResultDataAccessException: {}", e.getMessage());
        }
        log.info("(handleConcurrency) aggregateID for lock: {}", aggregateId);
    }

    @NewSpan
    private Optional<Snapshot> loadSnapshot(@SpanTag("aggregateId") String aggregateId) {
        return jdbcTemplate.query(LOAD_SNAPSHOT_QUERY, Map.of(AGGREGATE_ID, aggregateId), (rs, rowNum) -> Snapshot.builder()
                .aggregateId(rs.getString(AGGREGATE_ID))
                .aggregateType(rs.getString(AGGREGATE_TYPE))
                .data(rs.getBytes(DATA))
                .metaData(rs.getBytes(METADATA))
                .version(rs.getLong(VERSION))
                .timeStamp(rs.getTimestamp(TIMESTAMP).toLocalDateTime())
                .build()).stream().findFirst();
    }

    @NewSpan
    private <T extends AggregateRoot> T getAggregate(@SpanTag("aggregateId") final String aggregateId, @SpanTag("aggregateType") final Class<T> aggregateType) {
        try {
            return aggregateType.getConstructor(String.class).newInstance(aggregateId);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @NewSpan
    private <T extends AggregateRoot> T getSnapshotFromClass(@SpanTag("snapshot") Optional<Snapshot> snapshot, @SpanTag("aggregateId") String aggregateId, @SpanTag("aggregateType") Class<T> aggregateType) {
        if (snapshot.isEmpty()) {
            final var defaultSnapshot = EventSourcingUtils.snapshotFromAggregate(getAggregate(aggregateId, aggregateType));
            return EventSourcingUtils.aggregateFromSnapshot(defaultSnapshot, aggregateType);
        }
        return EventSourcingUtils.aggregateFromSnapshot(snapshot.get(), aggregateType);
    }

    @Override
    @Transactional(readOnly = true)
    @NewSpan
    public <T extends AggregateRoot> T load(@SpanTag("aggregateId") String aggregateId, @SpanTag("aggregateType") Class<T> aggregateType) {

        final Optional<Snapshot> snapshot = this.loadSnapshot(aggregateId);

        final var aggregate = this.getSnapshotFromClass(snapshot, aggregateId, aggregateType);

        final List<Event> events = this.loadEvents(aggregateId, aggregate.getVersion());
        events.forEach(event -> {
            aggregate.raiseEvent(event);
            log.info("raise event version: {}", event.getVersion());
        });

        if (aggregate.getVersion() == 0) throw new AggregateNotFoundException(aggregateId);

        log.info("(load) loaded aggregate: {}", aggregate);
        return aggregate;
    }

    @Override
    @NewSpan
    public Boolean exists(@SpanTag("aggregateId") String aggregateId) {
        try {
            final var id = jdbcTemplate.queryForObject(EXISTS_QUERY, Map.of(AGGREGATE_ID, aggregateId), String.class);
            log.info("aggregate exists id: {}", id);
            return true;
        } catch (Exception ex) {
            if (!(ex instanceof EmptyResultDataAccessException)) {
                throw new RuntimeException("exists", ex);
            }
            return false;
        }
    }
}
