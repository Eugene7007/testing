package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.OrderRequest;
import com.onlineshop.test.dto.response.OrderResponse;
import com.onlineshop.test.exception.OrderNotFoundException;
import com.onlineshop.test.mapper.OrderMapper;
import com.onlineshop.test.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class OrderService {

    OrderMapper orderMapper;
    OrderRepository orderRepository;

    public List<OrderResponse> getAllOrders() {
        return orderRepository
                .findAll()
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    public OrderResponse getOrderById(Long id) {
        return orderRepository
                .findById(id)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public OrderResponse createOrder(OrderRequest request) {
        var order = orderMapper.toEntity(request);
        orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    public OrderResponse updateOrder(Long id, OrderRequest request) {
        var existingOrder = orderRepository
                .findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        existingOrder.setAmount(request.getAmount());
        existingOrder.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(existingOrder);

        return orderMapper.toResponse(existingOrder);
    }

    public void deleteOrder(Long id) {
        orderRepository
                .findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        orderRepository.deleteById(id);
    }
}