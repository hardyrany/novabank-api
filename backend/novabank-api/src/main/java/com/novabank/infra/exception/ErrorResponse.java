package com.novabank.infra.exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private final String message;
    private final int status;
    private final String error;
    private final String path;
    private final LocalDateTime timestamp;

    private ErrorResponse(Builder builder) {
        this.message = builder.message;
        this.status = builder.status;
        this.error = builder.error;
        this.path = builder.path;
        this.timestamp = builder.timestamp;
    }

    public static class Builder {
        private String message;
        private int status;
        private String error;
        private String path;
        private LocalDateTime timestamp;

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ErrorResponse build() {
            if (timestamp == null) {
                timestamp = LocalDateTime.now();
            }
            return new ErrorResponse(this);
        }
    }

}
