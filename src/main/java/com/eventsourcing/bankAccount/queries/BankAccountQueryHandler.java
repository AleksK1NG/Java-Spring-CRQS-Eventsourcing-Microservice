package com.eventsourcing.bankAccount.queries;

import com.eventsourcing.bankAccount.domain.BankAccountAggregate;
import com.eventsourcing.bankAccount.domain.BankAccountDocument;
import com.eventsourcing.bankAccount.dto.BankAccountResponseDTO;
import com.eventsourcing.bankAccount.repository.BankAccountMongoRepository;
import com.eventsourcing.es.EventStoreDB;
import com.eventsourcing.mappers.BankAccountMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@Service
public class BankAccountQueryHandler implements BankAccountQueryService {

    private final EventStoreDB eventStoreDB;
    private final BankAccountMongoRepository mongoRepository;
    private static final String SERVICE_NAME = "microservice";

    @Override
    @NewSpan
    @Retry(name = SERVICE_NAME)
    @CircuitBreaker(name = SERVICE_NAME)
    public BankAccountResponseDTO handle(@SpanTag("query") GetBankAccountByIDQuery query) {
        Optional<BankAccountDocument> optionalDocument = mongoRepository.findByAggregateId(query.aggregateID());
        if (optionalDocument.isPresent()) {
            return BankAccountMapper.bankAccountResponseDTOFromDocument(optionalDocument.get());
        }

        final var aggregate = eventStoreDB.load(query.aggregateID(), BankAccountAggregate.class);
        final var savedDocument = mongoRepository.save(BankAccountMapper.bankAccountDocumentFromAggregate(aggregate));
        log.info("(GetBankAccountByIDQuery) savedDocument: {}", savedDocument);

        final var bankAccountResponseDTO = BankAccountMapper.bankAccountResponseDTOFromAggregate(aggregate);
        log.info("(GetBankAccountByIDQuery) response: {}", bankAccountResponseDTO);
        return bankAccountResponseDTO;
    }

    @Override
    @NewSpan
    @Retry(name = SERVICE_NAME)
    @CircuitBreaker(name = SERVICE_NAME)
    public Page<BankAccountResponseDTO> handle(@SpanTag("query") FindAllOrderByBalance query) {
        return mongoRepository.findAll(PageRequest.of(query.page(), query.size(), Sort.by("balance")))
                .map(BankAccountMapper::bankAccountResponseDTOFromDocument);
    }
}
