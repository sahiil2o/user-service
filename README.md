# User Service

A Spring Boot microservice responsible for managing employees in the Employee Access Management System.
Communicates with role-service to validate roles, and logs all operations to MongoDB as audit trails.

## Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA
- Spring Data MongoDB
- Spring Web MVC
- MySQL (employee data)
- MongoDB (audit logs)
- RestTemplate (inter-service communication)
- Lombok
- JUnit 5 + Mockito

## Architecture

This service is part of a two-microservice system:
```
user-service (port 8080)  →  role-service (port 8081)
        REST (HTTP)
```

Before creating or updating an employee, user-service calls role-service
to verify the role exists. All create, update, and delete operations are
logged to MongoDB as audit entries.

## Prerequisites

- Java 17+
- Maven
- MySQL running on port 3306
- MongoDB running on port 27017
- role-service running on port 8081

## Setup

1. Create the database:
```sql
CREATE DATABASE employee_db;
```

2. Make sure MongoDB is running locally:
```bash
mongod
```

3. Set the environment variable for DB password:
```bash
# Windows
set DB_PASSWORD=your_password

# Mac/Linux
export DB_PASSWORD=your_password
```

4. Start role-service first, then run this service:
```bash
./mvnw spring-boot:run
```

The service starts on **http://localhost:8080**

## REST API Endpoints

### Employee Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/employees` | Create a new employee |
| GET | `/api/employees` | Get all employees |
| GET | `/api/employees/{id}` | Get employee by ID |
| PUT | `/api/employees/{id}` | Update an employee |
| DELETE | `/api/employees/{id}` | Delete an employee |
| GET | `/api/employees/audit-logs` | Get all audit logs |

### Sample Request — Create Employee
```json
POST /api/employees
{
  "name": "Ashish",
  "email": "ashish@test.com",
  "department": "IT",
  "role": "ADMIN_ROLE"
}
```

### Sample Response
```json
{
  "id": 1,
  "name": "Ashish",
  "email": "ashish@test.com",
  "department": "IT",
  "role": "ADMIN_ROLE"
}
```

### Sample Audit Log Response
```json
{
  "id": "64f1a2b3c4d5e6f7a8b9c0d1",
  "action": "CREATE",
  "employeeName": "Ashish",
  "performedBy": "system",
  "timestamp": "2025-01-01T10:30:00"
}
```

## Error Handling

| Scenario | HTTP Status |
|----------|-------------|
| Employee not found | 404 Not Found |
| Duplicate email | 409 Conflict |
| Role not found in role-service | 404 Not Found |
| Validation failure | 400 Bad Request |

## Design Decisions

- **Dual database** — MySQL stores structured employee data while MongoDB stores
  audit logs, demonstrating the right tool for the right job
- **DTOs** — request and response objects are separate from JPA entities,
  preventing internal model exposure
- **Role validation via REST** — user-service calls role-service before persisting
  an employee, ensuring referential integrity across microservices
- **Audit logging** — every create, update, and delete is recorded in MongoDB
  with a timestamp, action type, and employee name

## Testing
```bash
./mvnw test
```

9 unit tests covering all service layer operations using JUnit 5 and Mockito.
No database required to run tests.