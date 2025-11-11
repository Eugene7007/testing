package com.onlineshop.test.mapper;

import com.onlineshop.test.dto.request.ProductRequest;
import com.onlineshop.test.dto.response.ProductResponse;
import com.onlineshop.test.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductRequest request);
}