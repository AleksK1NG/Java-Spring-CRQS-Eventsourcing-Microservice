package com.eventsourcing.bankAccount.queries;

import com.eventsourcing.bankAccount.domain.BankAccountAggregate;
import com.eventsourcing.bankAccount.dto.BankAccountResponseDTO;
import com.eventsourcing.es.EventStoreDB;
import com.eventsourcing.mappers.BankAccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@RequiredArgsConstructor
@Service
public class BankAccountQueryHandler implements BankAccountQueryService {

    private final EventStoreDB eventStoreDB;

    @Override
    public BankAccountResponseDTO handle(GetBankAccountByIDQuery query) {
        final var aggregate = eventStoreDB.load(query.aggregateID(), BankAccountAggregate.class);
        final var bankAccountResponseDTO = BankAccountMapper.bankAccountResponseDTOFromAggregate(aggregate);
        log.info("(GetBankAccountByIDQuery) response: {}", bankAccountResponseDTO);
        return bankAccountResponseDTO;
    }
}
