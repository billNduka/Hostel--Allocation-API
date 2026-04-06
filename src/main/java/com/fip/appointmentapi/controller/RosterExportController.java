package com.fip.appointmentapi.controller;

import com.fip.appointmentapi.service.RosterExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class RosterExportController {

    private final RosterExportService rosterExportService;

    @GetMapping("/roster")
    public ResponseEntity<byte[]> exportRoster(
            @RequestParam(required = false) Integer cycleId) {

        byte[] csv = rosterExportService.exportRosterAsCsv(cycleId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"roster.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/waitlist")
    public ResponseEntity<byte[]> exportWaitlist() {

        byte[] csv = rosterExportService.exportWaitlistAsCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"waitlist.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}