package com.eventsourcing.bankAccount.queries;

import com.eventsourcing.bankAccount.dto.BankAccountResponseDTO;
import org.springframework.data.domain.Page;

public interface BankAccountQueryService {
    BankAccountResponseDTO handle(GetBankAccountByIDQuery query);
    Page<BankAccountResponseDTO> handle(FindAllOrderByBalance query);
}
