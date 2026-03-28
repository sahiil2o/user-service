package com.sahil.user_service.service;

import com.sahil.user_service.dto.EmployeeRequest;
import com.sahil.user_service.dto.EmployeeResponse;

import java.util.List;

public interface EmployeeService {
    EmployeeResponse createEmployee(EmployeeRequest employeeRequest);
    EmployeeResponse getEmployeeById(Long id);
    List<EmployeeResponse> getAllEmployees();
    EmployeeResponse updateEmployee(Long id, EmployeeRequest employeeRequest);
    void  deleteEmployee(Long id);
}
