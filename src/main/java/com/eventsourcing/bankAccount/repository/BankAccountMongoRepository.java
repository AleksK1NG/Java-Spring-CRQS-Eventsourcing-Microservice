package com.eventsourcing.bankAccount.repository;

import com.eventsourcing.bankAccount.domain.BankAccountDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BankAccountMongoRepository extends MongoRepository<BankAccountDocument, String> {

    Optional<BankAccountDocument> findByAggregateId(String aggregateId);

    void deleteByAggregateId(String aggregateId);
}
