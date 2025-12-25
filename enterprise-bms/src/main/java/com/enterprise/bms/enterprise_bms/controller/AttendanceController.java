package com.enterprise.bms.enterprise_bms.controller;

import com.enterprise.bms.enterprise_bms.dto.AttendanceDTO;
import com.enterprise.bms.enterprise_bms.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<AttendanceDTO> createAttendance(@RequestBody AttendanceDTO dto) {
        AttendanceDTO saved = attendanceService.saveAttendance(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<AttendanceDTO>> getFilteredAttendances(
            @RequestParam(required = false) String recipientType,
            @RequestParam(required = false) Long recipientId,
            @RequestParam(required = false) String month) {
        return ResponseEntity.ok(attendanceService.getFilteredAttendances(recipientType, recipientId, month));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceDTO> getAttendanceById(@PathVariable Long id) {
        AttendanceDTO attendance = attendanceService.getAttendanceById(id);
        return ResponseEntity.ok(attendance);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttendanceDTO> updateAttendance(@PathVariable Long id, @RequestBody AttendanceDTO dto) {
        AttendanceDTO updated = attendanceService.updateAttendance(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.noContent().build();
    }
}