package com.kos.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /* =========================
       Custom validation errors
       ========================= */
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInput(
            InvalidInputException ex) {

        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /* =========================
       Not Found / Status errors
       ========================= */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleStatusException(
            ResponseStatusException ex) {

        ErrorResponse error = new ErrorResponse(
                ex.getReason(),
                ex.getStatusCode().value()
        );

        return new ResponseEntity<>(error, ex.getStatusCode());
    }

    /* =========================
       SSE client disconnect or timeout — swallow silently.
       The response is already committed as text/event-stream;
       writing a body (e.g. ErrorResponse JSON) would throw
       HttpMessageNotWritableException — so we return void.
       ========================= */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public ResponseEntity<Void> handleSseDisconnect(AsyncRequestNotUsableException ex) {
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<Void> handleSseTimeout(AsyncRequestTimeoutException ex) {
        // Normal SSE lifecycle — client will auto-reconnect
        return ResponseEntity.noContent().build();
    }

    /* =========================
       Fallback (ANY exception)
       ========================= */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        // Skip logging for routine SSE lifecycle exceptions to reduce noise
        String msg = ex.getMessage();
        if (msg == null || !msg.contains("event-stream")) {
            ex.printStackTrace();
        }
        ErrorResponse error = new ErrorResponse(
                "Internal server error: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
