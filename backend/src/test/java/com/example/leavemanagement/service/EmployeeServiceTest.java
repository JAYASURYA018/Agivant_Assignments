package com.example.leavemanagement.service;

import com.example.leavemanagement.dto.*;
import com.example.leavemanagement.entity.Employee;
import com.example.leavemanagement.exception.*;
import com.example.leavemanagement.repository.EmployeeRepository;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createEmployee_Success() {
        EmployeeRequest request = new EmployeeRequest("Rahul", "Sharma", "rahul.sharma@example.com", "password", "Engineering");
        
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
        
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setFirstName("Rahul");
        emp.setLastName("Sharma");
        emp.setEmail("rahul.sharma@example.com");
        emp.setDepartment("Engineering");
        emp.setCreatedAt(LocalDateTime.now());
        
        when(employeeRepository.save(any(Employee.class))).thenReturn(emp);

        EmployeeResponse response = employeeService.createEmployee(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Rahul", response.getFirstName());
        assertEquals("rahul.sharma@example.com", response.getEmail());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void createEmployee_DuplicateEmail_ThrowsBusinessException() {
        EmployeeRequest request = new EmployeeRequest("Rahul", "Sharma", "rahul.sharma@example.com", "password", "Engineering");
        
        when(employeeRepository.existsByEmail("rahul.sharma@example.com")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            employeeService.createEmployee(request);
        });

        assertEquals("An employee with email 'rahul.sharma@example.com' already exists.", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void getAllEmployees_Success() {
        Employee e1 = new Employee();
        e1.setId(1L);
        e1.setFirstName("Rahul");
        e1.setLastName("Sharma");
        e1.setEmail("rahul.sharma@example.com");
        e1.setDepartment("Engineering");

        Employee e2 = new Employee();
        e2.setId(2L);
        e2.setFirstName("Amit");
        e2.setLastName("Verma");
        e2.setEmail("amit.verma@example.com");
        e2.setDepartment("HR");

        when(employeeRepository.findAll()).thenReturn(Arrays.asList(e1, e2));

        List<EmployeeResponse> list = employeeService.getAllEmployees();

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("Rahul", list.get(0).getFirstName());
        assertEquals("Amit", list.get(1).getFirstName());
    }

    @Test
    void getEmployeeById_Success() {
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setFirstName("Rahul");
        emp.setLastName("Sharma");
        emp.setEmail("rahul.sharma@example.com");
        emp.setDepartment("Engineering");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));

        EmployeeResponse response = employeeService.getEmployeeById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Rahul", response.getFirstName());
    }

    @Test
    void getEmployeeById_NotFound_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(99L);
        });

        assertEquals("Employee with ID 99 not found.", exception.getMessage());
    }

    @Test
    void login_Success() {
        com.example.leavemanagement.dto.LoginRequest loginReq = new com.example.leavemanagement.dto.LoginRequest("rahul.sharma@example.com", "password");
        
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setFirstName("Rahul");
        emp.setLastName("Sharma");
        emp.setEmail("rahul.sharma@example.com");
        emp.setPassword(com.example.leavemanagement.util.PasswordUtil.hashPassword("password"));
        emp.setDepartment("Engineering");

        when(employeeRepository.findByEmail("rahul.sharma@example.com")).thenReturn(Optional.of(emp));

        EmployeeResponse response = employeeService.login(loginReq);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Rahul", response.getFirstName());
    }

    @Test
    void login_InvalidEmail_ThrowsBusinessException() {
        com.example.leavemanagement.dto.LoginRequest loginReq = new com.example.leavemanagement.dto.LoginRequest("unknown@example.com", "password");
        
        when(employeeRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            employeeService.login(loginReq);
        });

        assertEquals("Invalid email or password.", exception.getMessage());
    }

    @Test
    void login_WrongPassword_ThrowsBusinessException() {
        com.example.leavemanagement.dto.LoginRequest loginReq = new com.example.leavemanagement.dto.LoginRequest("rahul.sharma@example.com", "wrongpassword");
        
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setEmail("rahul.sharma@example.com");
        emp.setPassword(com.example.leavemanagement.util.PasswordUtil.hashPassword("password"));

        when(employeeRepository.findByEmail("rahul.sharma@example.com")).thenReturn(Optional.of(emp));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            employeeService.login(loginReq);
        });

        assertEquals("Invalid email or password.", exception.getMessage());
    }

    @Test
    void updateEmployee_Success() {
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setFirstName("Rahul");
        emp.setLastName("Sharma");
        emp.setEmail("rahul.sharma@example.com");
        emp.setDepartment("Engineering");
        emp.setPassword("old-hashed-password");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeRequest updateReq = new EmployeeRequest("Rahul", "Verma", "rahul.sharma@example.com", "", "Operations", "MANAGER");
        EmployeeResponse response = employeeService.updateEmployee(1L, updateReq);

        assertNotNull(response);
        assertEquals("Rahul", response.getFirstName());
        assertEquals("Verma", response.getLastName());
        assertEquals("Operations", response.getDepartment());
        assertEquals("MANAGER", response.getRole());
        verify(employeeRepository, times(1)).save(emp);
    }

    @Test
    void updateEmployee_EmailAlreadyExists_ThrowsBusinessException() {
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setEmail("rahul.sharma@example.com");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeRepository.existsByEmail("other@example.com")).thenReturn(true);

        EmployeeRequest updateReq = new EmployeeRequest("Rahul", "Sharma", "other@example.com", "", "Engineering", "EMPLOYEE");
        
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            employeeService.updateEmployee(1L, updateReq);
        });

        assertEquals("An employee with email 'other@example.com' already exists.", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployee_Success() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(1L);

        assertDoesNotThrow(() -> {
            employeeService.deleteEmployee(1L);
        });

        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteEmployee_NotFound_ThrowsResourceNotFoundException() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.deleteEmployee(99L);
        });

        assertEquals("Employee with ID 99 not found.", exception.getMessage());
        verify(employeeRepository, never()).deleteById(anyLong());
    }
}
