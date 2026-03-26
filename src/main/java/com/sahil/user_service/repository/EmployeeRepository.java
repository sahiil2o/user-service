package com.sahil.user_service.repository;

import com.sahil.user_service.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EmployeeRepository extends JpaRepository<Employee,Long>{
    boolean existsByEmail(String email);
}
