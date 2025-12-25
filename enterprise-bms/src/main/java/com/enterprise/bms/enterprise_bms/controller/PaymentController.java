package com.enterprise.bms.enterprise_bms.controller;

import com.enterprise.bms.enterprise_bms.dto.PaymentsDTO;
import com.enterprise.bms.enterprise_bms.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentsDTO> createPayment(@RequestBody PaymentsDTO dto) {
        PaymentsDTO saved = paymentService.savePayment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<PaymentsDTO>> getFilteredPayments(
            @RequestParam(required = false) String recipientType,
            @RequestParam(required = false) Long recipientId,
            @RequestParam(required = false) String month) {
        return ResponseEntity.ok(paymentService.getFilteredPayments(recipientType, recipientId, month));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentsDTO> getPaymentById(@PathVariable Long id) {
        PaymentsDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentsDTO> updatePayment(@PathVariable Long id, @RequestBody PaymentsDTO dto) {
        PaymentsDTO updated = paymentService.updatePayment(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}