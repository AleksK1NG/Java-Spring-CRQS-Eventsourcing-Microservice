package com.eventsourcing.filters;

import com.eventsourcing.bankAccount.exceptions.BankAccountDocumentNotFoundException;
import com.eventsourcing.bankAccount.exceptions.InvalidAddressException;
import com.eventsourcing.bankAccount.exceptions.InvalidEmailException;
import com.eventsourcing.es.exceptions.AggregateNotFoundException;
import com.eventsourcing.exceptions.InternalServerErrorResponse;
import com.eventsourcing.exceptions.ExceptionResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor

@Order(2)
public class GlobalControllerAdvice {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<InternalServerErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        final var response = new InternalServerErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), LocalDateTime.now().toString());
        log.error("RuntimeException response: {} ", response);
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleInvalidArgument(MethodArgumentNotValidException ex) {
        final Map<String, String> errorMap = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errorMap.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({AggregateNotFoundException.class, BankAccountDocumentNotFoundException.class})
    public ResponseEntity<ExceptionResponseDTO> handleAggregateNotFoundExceptions(AggregateNotFoundException ex) {
        final var notFoundResponseDTO = new ExceptionResponseDTO(HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now());
        log.error("handleAggregateNotFoundExceptions response ex:", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponseDTO);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({InvalidAddressException.class, InvalidEmailException.class})
    public ResponseEntity<ExceptionResponseDTO> handleInvalidAggregateExceptions(AggregateNotFoundException ex) {
        final var notFoundResponseDTO = new ExceptionResponseDTO(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), LocalDateTime.now());
        log.error("handleInvalidAggregateExceptions response ex:", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(notFoundResponseDTO);
    }
}
