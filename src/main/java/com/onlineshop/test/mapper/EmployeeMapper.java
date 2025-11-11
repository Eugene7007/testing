package com.onlineshop.test.mapper;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Employee;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {DepartmentMapper.class})
public interface EmployeeMapper {

    @Mapping(source = "department.name", target = "departmentName")
    @Mapping(source = "manager.name", target = "managerName", defaultValue = "Нет менеджера")
    EmployeeResponse toResponse(Employee employee);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "departmentId", target = "department.id")
    @Mapping(source = "managerId", target = "manager.id")
    Employee toEntity(EmployeeRequest request);
}