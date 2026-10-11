package com.novabank.features.user.dto;

public class ResetPasswordResponse {

    private String temporaryPassword;

    public ResetPasswordResponse() {}

    public ResetPasswordResponse(String temporaryPassword) {
        this.temporaryPassword = temporaryPassword;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    public void setTemporaryPassword(String temporaryPassword) {
        this.temporaryPassword = temporaryPassword;
    }
}
