package com.novabank.features.customer.dto;

import java.time.LocalDateTime;

public class CustomerResponse {

    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String phone;
    private String documentNumber;
    private String documentType;
    private LocalDateTime birthDate;
    private String address;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
