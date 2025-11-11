package com.onlineshop.test.service;

import com.onlineshop.test.dto.response.DepartmentResponse;
import com.onlineshop.test.entity.Department;
import com.onlineshop.test.exception.DepartmentNotFoundException;
import com.onlineshop.test.mapper.DepartmentMapper;
import com.onlineshop.test.repository.DepartmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Unit tests
@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Spy
    DepartmentMapper departmentMapper;
    @Mock
    DepartmentRepository departmentRepository;

    @InjectMocks
    DepartmentService departmentService;

    @Captor
    ArgumentCaptor<Department> departmentCaptor;

    @Test
    @DisplayName("getById")
    void getDepartmentById_ShouldReturnDepartmentResponse_WhenDepartmentExists() {
        // Arrange
        var departmentId = 1L;

        var departmentEntity = new Department();
        var departmentResponse = new DepartmentResponse(1L, "Department_01", "loc 01");

        when(departmentRepository.findById(departmentId))
                .thenReturn(Optional.of(departmentEntity));

        when(departmentMapper.toResponse(departmentEntity))
                .thenReturn(departmentResponse);

        // Act
        var result = departmentService.getDepartmentById(departmentId);

        // Assert

        // assertNotNull(result);
        // assertEquals(departmentResponse, result);

        assertThat(result).isNotNull();
        assertThat(departmentResponse)
                .isNotNull()
                .isEqualTo(result);

        // Verify
        verify(departmentRepository, times(1)).findById(departmentId);
        verify(departmentMapper, times(1)).toResponse(departmentEntity);
        verify(departmentMapper, times(1)).toResponse(departmentCaptor.capture());
    }

    @Test
    void getDepartmentById_ShouldThrowDepartmentNotFoundException_WhenDepartmentDoesNotExist() {
        // Arrange
        var departmentId = 1L;

        when(departmentRepository.findById(departmentId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(DepartmentNotFoundException.class, () -> departmentService.getDepartmentById(departmentId));

        // Verify
        verify(departmentRepository, times(1)).findById(departmentId);
        verifyNoInteractions(departmentMapper);
    }
}