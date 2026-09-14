package com.novabank.features.transfer.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.novabank.features.transfer.dto.TransferRequest;
import com.novabank.features.transfer.service.TransferService;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<Void> transfer(@RequestBody TransferRequest request) {
        transferService.transfer(request.getSourceAccountId(), request.getTargetAccountId(),
                request.getAmount(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
