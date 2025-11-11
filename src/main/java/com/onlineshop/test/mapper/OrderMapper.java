package com.onlineshop.test.mapper;

import com.onlineshop.test.dto.request.OrderRequest;
import com.onlineshop.test.dto.response.OrderResponse;
import com.onlineshop.test.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {EmployeeMapper.class, CustomerMapper.class})
public interface OrderMapper {

    @Mapping(source = "employee.name", target = "employeeName")
    @Mapping(source = "customer.name", target = "customerName")
    OrderResponse toResponse(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(source = "employeeId", target = "employee.id")
    @Mapping(source = "customerId", target = "customer.id")
    Order toEntity(OrderRequest request);
}