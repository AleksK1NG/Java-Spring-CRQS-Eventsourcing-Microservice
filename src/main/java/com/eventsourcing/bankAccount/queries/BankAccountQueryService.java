package com.eventsourcing.bankAccount.queries;

import com.eventsourcing.bankAccount.dto.BankAccountResponseDTO;

public interface BankAccountQueryService {
    BankAccountResponseDTO handle(GetBankAccountByIDQuery query);
}
