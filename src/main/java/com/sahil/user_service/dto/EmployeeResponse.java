package com.sahil.user_service.dto;
import lombok.Data;

@Data
public class EmployeeResponse {
    private Long id;
    private String name;
    private String email;
    private String department;
    private String role;
}
