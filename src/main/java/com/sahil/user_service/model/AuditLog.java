package com.sahil.user_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "audit_logs")
public class AuditLog {
    @Id
    private String id;

    private String action;
    private String employeeName;
    private String performedBy;
    private LocalDateTime timestamp;

    public AuditLog(String action,String employeeName) {
        this.action = action;
        this.employeeName = employeeName;
        this.performedBy = "system";
        this.timestamp = LocalDateTime.now();
    }
}
