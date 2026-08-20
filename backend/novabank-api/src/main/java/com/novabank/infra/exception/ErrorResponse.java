package com.novabank.infra.exception;

import java.time.LocalDateTime;

public class ErrorResponse {
    
    // Instance final fields
    private final String message;
    private final int status;
    private final String error;
    private final String path;
    private final LocalDateTime timestamp;

}
