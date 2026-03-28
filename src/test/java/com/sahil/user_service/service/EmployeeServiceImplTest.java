package com.sahil.user_service.service;

import com.sahil.user_service.dto.EmployeeRequest;
import com.sahil.user_service.dto.EmployeeResponse;
import com.sahil.user_service.dto.RoleResponse;
import com.sahil.user_service.exception.DuplicateEmailException;
import com.sahil.user_service.exception.ResourceNotFoundException;
import com.sahil.user_service.model.AuditLog;
import com.sahil.user_service.model.Employee;
import com.sahil.user_service.repository.AuditLogRepository;
import com.sahil.user_service.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeRequest request;
    private Employee employee;
    private RoleResponse roleResponse;

    @BeforeEach
    void setUp() {
        // inject the @Value field manually
        org.springframework.test.util.ReflectionTestUtils.setField(
                employeeService, "roleServiceBaseUrl", "http://localhost:8081"
        );

        request = new EmployeeRequest();
        request.setName("Ashish");
        request.setEmail("ashish@test.com");
        request.setDepartment("IT");
        request.setRole("DEV_ROLE");

        employee = new Employee();
        employee.setId(1L);
        employee.setName("Ashish");
        employee.setEmail("ashish@test.com");
        employee.setDepartment("IT");
        employee.setRole("DEV_ROLE");

        roleResponse = new RoleResponse();
        roleResponse.setId(1L);
        roleResponse.setName("ADMIN");
    }

    // --- createEmployee ---

    @Test
    void createEmployee_success() {
       when(employeeRepository.existsByEmail(request.getEmail())).thenReturn(false);
       when(restTemplate.getForObject(anyString(),eq(RoleResponse.class),anyString()))
               .thenReturn(roleResponse);
       when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
       when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

       EmployeeResponse response = employeeService.createEmployee(request);

       assertNotNull(response);
       assertEquals("Ashish", response.getName());
       assertEquals("ashish@test.com", response.getEmail());
       verify(employeeRepository, times(1)).save(any(Employee.class));
       verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }
    @Test
    void createEmployee_duplicateEmail_throwsException() {
        when(employeeRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(DuplicateEmailException.class,
                () -> employeeService.createEmployee(request));

        verify(employeeRepository, never()).save(any());
    }
    @Test
    void createEmployee_roleNotFound_throwsException() {
        when(employeeRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(restTemplate.getForObject(anyString(), eq(RoleResponse.class), anyString()))
                .thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.createEmployee(request));

        verify(employeeRepository, never()).save(any());
    }

    // --- getEmployeeById ---

    @Test
    void getEmployeeById_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeeResponse response = employeeService.getEmployeeById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Ashish", response.getName());
    }

    @Test
    void getEmployeeById_notFound_throwsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getEmployeeById(99L));
    }
    // --- updateEmployee ---

    @Test
    void updateEmployee_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(restTemplate.getForObject(anyString(), eq(RoleResponse.class), anyString()))
                .thenReturn(roleResponse);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponse response = employeeService.updateEmployee(1L, request);

        assertNotNull(response);
        assertEquals("Ashish", response.getName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void updateEmployee_duplicateEmail_throwsException() {
        Employee existing = new Employee();
        existing.setId(1L);
        existing.setEmail("old@example.com");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(DuplicateEmailException.class,
                () -> employeeService.updateEmployee(1L, request));

        verify(employeeRepository, never()).save(any());
    }
    // --- deleteEmployee ---
    @Test
    void deleteEmployee_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(1L);

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
        verify(employeeRepository, times(1)).deleteById(1L);
    }
    @Test
    void deleteEmployee_notFound_throwsException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.deleteEmployee(99L));

        verify(employeeRepository, never()).deleteById(any());
    }

}
