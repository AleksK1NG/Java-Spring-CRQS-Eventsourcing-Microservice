package com.eventsourcing.bankAccount.delivery;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/bank")
@Slf4j
public class BankAccountController {


    @GetMapping("{aggregateId}")
    public ResponseEntity<?> getBankAccount(@PathVariable String aggregateId) {
        log.info("GET bank account: {}", aggregateId);
        return ResponseEntity.ok("OK");
    }
}
