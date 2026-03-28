package com.sahil.user_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogResponse {
    private String id;
    private String action;
    private String employeeName;
    private String performedBy;
    private LocalDateTime timestamp;
}
