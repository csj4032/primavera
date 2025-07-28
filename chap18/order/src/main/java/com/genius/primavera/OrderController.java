package com.genius.primavera;

import com.genius.primavera.dto.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderServiceImpl orderService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("주문 생성 요청: customerId={}, items={}", 
                request.getCustomerId(), request.getItems().size());
        
        return orderService.createOrder(request);
    }
    
    @GetMapping("/{orderId}")
    public Mono<Order> getOrder(@PathVariable String orderId) {
        return orderService.getOrderById(orderId);
    }
    
    @PostMapping("/{orderId}/cancel")
    public Mono<Order> cancelOrder(@PathVariable String orderId, 
                                   @RequestParam(defaultValue = "사용자 요청") String reason) {
        log.info("주문 취소 요청: orderId={}, reason={}", orderId, reason);
        
        return orderService.cancelOrder(orderId, reason);
    }
}