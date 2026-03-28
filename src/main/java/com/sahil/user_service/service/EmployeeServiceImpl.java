package com.sahil.user_service.service;

import com.sahil.user_service.dto.AuditLogResponse;
import com.sahil.user_service.dto.EmployeeRequest;
import com.sahil.user_service.dto.EmployeeResponse;
import com.sahil.user_service.dto.RoleResponse;
import com.sahil.user_service.exception.DuplicateEmailException;
import com.sahil.user_service.exception.ResourceNotFoundException;
import com.sahil.user_service.model.AuditLog;
import com.sahil.user_service.model.Employee;
import com.sahil.user_service.repository.AuditLogRepository;
import com.sahil.user_service.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request){
        //check if Email is Duplicate before modifying emp
        if(employeeRepository.existsByEmail(request.getEmail())){
            throw new DuplicateEmailException("Employee with email already exists");
        }
        //validate role before saving
        RoleResponse role = restTemplate.getForObject(
                roleServiceBaseUrl + "/api/roles/name/{name}",
                RoleResponse.class,
                request.getRole()
        );

        if (role == null) {
            throw new ResourceNotFoundException("Role not found: " + request.getRole());
        }
        Employee emp = new Employee();
        emp.setName(request.getName());
        emp.setEmail(request.getEmail());
        emp.setDepartment(request.getDepartment());
        emp.setRole(request.getRole());

        Employee saved = employeeRepository.save(emp);

        auditLogRepository.save(new AuditLog("CREATE",saved.getName()));

        return toResponse(saved);
    }
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees(){
        return employeeRepository.findAll()
                .stream().map(this::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id){
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Employee not found with id " + id
                ));
        return toResponse(emp);
    }
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request){
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Employee not found with id " + id
                ));
        //check if Email is Duplicate before modifying emp
        if (!emp.getEmail().equals(request.getEmail())
                && employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Employee with email already exists");
        }
        //validate role before saving
        RoleResponse role = restTemplate.getForObject(
                roleServiceBaseUrl + "/api/roles/name/{name}",
                RoleResponse.class,
                request.getRole()
        );
        if (role == null) {
            throw new ResourceNotFoundException("Role not found: " + request.getRole());
        }
        emp.setName(request.getName());
        emp.setEmail(request.getEmail());
        emp.setDepartment(request.getDepartment());
        emp.setRole(request.getRole());

        Employee updated = employeeRepository.save(emp);
        auditLogRepository.save(new AuditLog("UPDATE",updated.getName()));

            return toResponse(updated);
    }

    @Transactional
    public void deleteEmployee(Long id){
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Employee not found with id " + id
                ));
        auditLogRepository.save(new AuditLog("DELETE",emp.getName()));
        employeeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogs(){
        return auditLogRepository.findAll()
                .stream()
                .map(log ->{
                    AuditLogResponse res = new AuditLogResponse();
                    res.setId(log.getId());
                    res.setAction(log.getAction());
                    res.setEmployeeName(log.getEmployeeName());
                    res.setPerformedBy(log.getPerformedBy());
                    res.setTimestamp(log.getTimestamp());
                    return res;
                })
                .toList();
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
