package com.secureshare.controller;

import com.secureshare.entity.AuditLog;
import com.secureshare.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditLogService auditLogService;

    public AuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/my-logs")
    public ResponseEntity<List<AuditLog>> myLogs(Authentication auth) {
        return ResponseEntity.ok(auditLogService.getUserLogs(auth.getName()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> allLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    @GetMapping("/verify-chain")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> verifyChain() {
        boolean intact = auditLogService.verifyChainIntegrity();
        return ResponseEntity.ok(Map.of(
                "chainIntact", intact,
                "message", intact
                        ? "Audit log chain is intact - no tampering detected"
                        : "CHAIN BROKEN - log tampering detected!"
        ));
    }

    @GetMapping("/honey-alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> honeyAlerts() {
        return ResponseEntity.ok(auditLogService.getHoneyAlerts());
    }
}
