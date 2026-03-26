package com.sahil.user_service.repository;

import com.sahil.user_service.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepository extends MongoRepository<AuditLog,String>{

}
