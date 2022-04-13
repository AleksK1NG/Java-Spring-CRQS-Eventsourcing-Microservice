package com.eventsourcing.exceptions;

public record InternalServerErrorResponse(int status, String message, String timestamp) {
}
