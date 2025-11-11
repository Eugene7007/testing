package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.ProductRequest;
import com.onlineshop.test.dto.response.ProductResponse;
import com.onlineshop.test.entity.Product;
import com.onlineshop.test.exception.ProductNotFoundException;
import com.onlineshop.test.mapper.ProductMapper;
import com.onlineshop.test.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductService {

    ProductMapper productMapper;
    ProductRepository productRepository;

    // Получение всех продуктов
    public List<ProductResponse> getAllProducts() {
        return productRepository
                .findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    // Получение продукта по ID
    public ProductResponse getProductById(Long id) {
        return productRepository
                .findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    // Создание нового продукта
    public ProductResponse createProduct(ProductRequest request) {
        var product = productMapper.toEntity(request);
        productRepository.save(product);

        return productMapper.toResponse(product);
    }

    // Обновление продукта
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        var existingProduct = productRepository
                .findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        existingProduct.setName(request.getName());
        existingProduct.setPrice(request.getPrice());

        productRepository.save(existingProduct);

        return productMapper.toResponse(existingProduct);
    }

    // Удаление продукта
    public void deleteProduct(Long id) {
        productRepository
                .findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        productRepository.deleteById(id);
    }
}