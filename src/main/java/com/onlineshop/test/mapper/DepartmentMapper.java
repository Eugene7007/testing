package com.onlineshop.test.mapper;

import com.onlineshop.test.dto.request.DepartmentRequest;
import com.onlineshop.test.dto.response.DepartmentResponse;
import com.onlineshop.test.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    DepartmentResponse toResponse(Department department);

    @Mapping(target = "id", ignore = true)
    Department toEntity(DepartmentRequest request);
}