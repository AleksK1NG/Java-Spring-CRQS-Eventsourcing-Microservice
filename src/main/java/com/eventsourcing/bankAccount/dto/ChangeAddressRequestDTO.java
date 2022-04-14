package com.eventsourcing.bankAccount.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record ChangeAddressRequestDTO(@NotBlank @Size(min = 10, max = 250) String newAddress) {
}
