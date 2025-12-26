package com.enterprise.bms.enterprise_bms.controller;

import com.enterprise.bms.enterprise_bms.dto.AdvanceDTO;
import com.enterprise.bms.enterprise_bms.service.AdvanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/advances")
public class AdvanceController {
    private final AdvanceService advanceService;

    @PostMapping
    public ResponseEntity<AdvanceDTO> createAdvance(@RequestBody AdvanceDTO dto) {
        AdvanceDTO saved = advanceService.saveAdvance(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<AdvanceDTO>> getFilteredAdvances(
            @RequestParam(required = false) String recipientType,
            @RequestParam(required = false) Long recipientId,
            @RequestParam(required = false) String month) {
        return ResponseEntity.ok(advanceService.getFilteredAdvances(recipientType, recipientId, month));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvanceDTO> getAdvanceById(@PathVariable Long id) {
        AdvanceDTO advance = advanceService.getAdvanceById(id);
        return ResponseEntity.ok(advance);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdvanceDTO> updateAdvance(@PathVariable Long id, @RequestBody AdvanceDTO dto) {
        AdvanceDTO updated = advanceService.updateAdvance(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdvance(@PathVariable Long id) {
        advanceService.deleteAdvance(id);
        return ResponseEntity.noContent().build();
    }
}