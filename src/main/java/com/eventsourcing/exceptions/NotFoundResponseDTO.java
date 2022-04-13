package com.eventsourcing.exceptions;

import java.time.LocalDateTime;

public record NotFoundResponseDTO(int Status, String message, LocalDateTime timestamp) {
}
