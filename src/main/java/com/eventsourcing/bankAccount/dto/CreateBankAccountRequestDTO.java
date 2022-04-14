package com.eventsourcing.bankAccount.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record CreateBankAccountRequestDTO(
        @Email @NotBlank @Size(min = 10, max = 250) String email,
        @NotBlank @Size(min = 10, max = 250) String address,
        @NotBlank @Size(min = 10, max = 250) String userName) {
}
