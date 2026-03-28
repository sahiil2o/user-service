package com.sahil.user_service.service;

import com.sahil.user_service.dto.EmployeeRequest;
import com.sahil.user_service.dto.EmployeeResponse;
import com.sahil.user_service.dto.RoleResponse;
import com.sahil.user_service.exceprion.ResourceNotFoundException;
import com.sahil.user_service.model.AuditLog;
import com.sahil.user_service.model.Employee;
import com.sahil.user_service.repository.AuditLogRepository;
import com.sahil.user_service.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AuditLogRepository auditLogRepository;

    private final RestTemplate restTemplate;
    @Value("${role.service.base-url}")
    private String roleServiceBaseUrl;

    public EmployeeResponse createEmployee(EmployeeRequest request){
        Employee emp = new Employee();
        emp.setName(request.getName());
        emp.setEmail(request.getEmail());
        emp.setDepartment(request.getDepartment());
        emp.setRole(request.getRole());
        RoleResponse role = restTemplate.getForObject(
                roleServiceBaseUrl + "/api/roles/name/{name}",
                RoleResponse.class,
                request.getRole()
        );

        if (role == null) {
            throw new ResourceNotFoundException("Role not found: " + request.getRole());
        }
        Employee saved = employeeRepository.save(emp);

        auditLogRepository.save(new AuditLog("CREATE",saved.getName()));

        return toResponse(saved);
    }

    public List<EmployeeResponse> getAllEmployees(){
        return employeeRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    public EmployeeResponse getEmployeeById(Long id){
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Employee not found with id " + id
                ));
        return toResponse(emp);
    }

    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request){
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Employee not found with id " + id
                ));
        emp.setName(request.getName());
        emp.setEmail(request.getEmail());
        emp.setDepartment(request.getDepartment());
        emp.setRole(request.getRole());
        RoleResponse role = restTemplate.getForObject(
                roleServiceBaseUrl + "/api/roles/name/{name}",
                RoleResponse.class,
                request.getRole()
        );

        if (role == null) {
            throw new ResourceNotFoundException("Role not found: " + request.getRole());
        }
        Employee updated = employeeRepository.save(emp);
        auditLogRepository.save(new AuditLog("UPDATE",updated.getName()));

            return toResponse(updated);
    }

    public void deleteEmployee(Long id){
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Employee not found with id " + id
                ));
        auditLogRepository.save(new AuditLog("DELETE",emp.getName()));
        employeeRepository.deleteById(id);
    }


    public List<AuditLog> getAuditLogs(){
        return auditLogRepository.findAll();
    }

    private EmployeeResponse toResponse(Employee emp){
        EmployeeResponse res = new EmployeeResponse();

        res.setId(emp.getId());
        res.setName(emp.getName());
        res.setEmail(emp.getEmail());
        res.setDepartment(emp.getDepartment());
        res.setRole(emp.getRole());
        return res;
    }

}
