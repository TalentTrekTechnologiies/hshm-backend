package com.hospital.appointment.controller;

import com.hospital.appointment.entity.PaymentConfig;
import com.hospital.appointment.repository.PaymentConfigRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/payment-config")
@CrossOrigin
public class PaymentConfigController {

    private final PaymentConfigRepository repository;

    public PaymentConfigController(PaymentConfigRepository repository) {
        this.repository = repository;
    }

    // Create or Update config (only 1 row allowed)
    @PostMapping
    public PaymentConfig save(@RequestBody PaymentConfig config) {

        PaymentConfig existing = repository.findAll()
                .stream()
                .findFirst()
                .orElse(null);

        if (existing != null) {
            config.setId(existing.getId());
        }

        return repository.save(config);
    }

    // Get current config
    @GetMapping
    public PaymentConfig get() {
        return repository.findAll()
                .stream()
                .findFirst()
                .orElse(null);
    }
}
