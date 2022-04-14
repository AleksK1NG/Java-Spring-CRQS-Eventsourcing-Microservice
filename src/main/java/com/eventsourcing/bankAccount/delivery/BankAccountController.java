package com.eventsourcing.bankAccount.delivery;


import com.eventsourcing.bankAccount.commands.*;
import com.eventsourcing.bankAccount.dto.*;
import com.eventsourcing.bankAccount.queries.BankAccountQueryService;
import com.eventsourcing.bankAccount.queries.GetBankAccountByIDQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/bank")
@Slf4j
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountCommandService commandService;
    private final BankAccountQueryService queryService;

    @GetMapping("{aggregateId}")
    public ResponseEntity<BankAccountResponseDTO> getBankAccount(@PathVariable String aggregateId) {
        final var query = new GetBankAccountByIDQuery(aggregateId);
        log.info("GET bank account query: {}", query);
        final var result = queryService.handle(query);
        log.info("GET bank account result: {}", result);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<String> createBankAccount(@Valid @RequestBody CreateBankAccountRequestDTO dto) {
        final var aggregateID = UUID.randomUUID().toString();
        final var command = new CreateBankAccountCommand(aggregateID, dto.email(), dto.userName(), dto.address());
        final var id = commandService.handle(command);
        log.info("CREATE bank account id: {}", id);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @PostMapping(path = "/deposit/{aggregateId}")
    public ResponseEntity<Void> depositAmount(@Valid @RequestBody DepositAmountRequestDTO dto, @PathVariable String aggregateId) {
        final var command = new DepositAmountCommand(aggregateId, dto.amount());
        commandService.handle(command);
        log.info("DepositAmountCommand command: {}", command);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/email/{aggregateId}")
    public ResponseEntity<Void> changeEmail(@Valid @RequestBody ChangeEmailRequestDTO dto, @PathVariable String aggregateId) {
        final var command = new ChangeEmailCommand(aggregateId, dto.newEmail());
        commandService.handle(command);
        log.info("ChangeEmailCommand command: {}", command);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/address/{aggregateId}")
    public ResponseEntity<Void> changeAddress(@Valid @RequestBody ChangeAddressRequestDTO dto, @PathVariable String aggregateId) {
        final var command = new ChangeAddressCommand(aggregateId, dto.newAddress());
        commandService.handle(command);
        log.info("changeAddress command: {}", command);
        return ResponseEntity.ok().build();
    }
}
