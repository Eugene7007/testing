package com.onlineshop.test.mapper;

import com.onlineshop.test.dto.request.CustomerRequest;
import com.onlineshop.test.dto.response.CustomerResponse;
import com.onlineshop.test.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerResponse toResponse(Customer customer);

    @Mapping(target = "id", ignore = true)
    Customer toEntity(CustomerRequest request);
}