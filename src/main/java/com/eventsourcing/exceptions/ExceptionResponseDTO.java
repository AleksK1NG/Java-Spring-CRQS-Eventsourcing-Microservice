package com.eventsourcing.exceptions;

import java.time.LocalDateTime;

public record ExceptionResponseDTO(int Status, String message, LocalDateTime timestamp) {
}
