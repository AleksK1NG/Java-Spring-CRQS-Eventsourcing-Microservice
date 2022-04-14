package com.eventsourcing.configuration;

import com.eventsourcing.bankAccount.domain.BankAccountDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import javax.annotation.PostConstruct;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class MongoConfiguration {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void mongoInit() {
        final var bankAccounts = mongoTemplate.getCollection("bankAccounts");
        final var aggregateIdIndex = mongoTemplate.indexOps(BankAccountDocument.class).ensureIndex(new Index("aggregateId", Sort.Direction.ASC).unique());
        final var indexInfo = mongoTemplate.indexOps(BankAccountDocument.class).getIndexInfo();
        log.info("MongoDB connected, bankAccounts aggregateId index created: {}", indexInfo);
    }
}
