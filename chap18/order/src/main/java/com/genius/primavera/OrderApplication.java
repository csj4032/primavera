package com.genius.primavera;

import com.genius.primavera.event.OrderEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.reactive.function.server.RouterFunction;

import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 18: 마이크로서비스)
 * =============================================================================
 * 
 * 1️⃣ 마이크로서비스 환경 확인:
 *    cd infrastructure
 *    docker-compose -f docker-compose.microservices.yml ps
 * 
 * 2️⃣ Order Service 실행:
 *    ./gradlew :chap18:order:bootRun -Dspring.profiles.active=local -Dserver.port=8083
 * 
 * 3️⃣ API 테스트:
 *    curl http://localhost:8083/users/1/orders
 *    curl http://localhost:8083/users/2/orders
 * 
 * 📊 기능:
 *    - 리액티브 주문 처리
 *    - Kafka 이벤트 발행
 *    - 함수형 엔드포인트
 * 
 * 🔗 연동 서비스:
 *    - MariaDB: localhost:3308
 *    - Kafka: localhost:9092 (주문 이벤트)
 *    - Redis: localhost:6380 (캐싱)
 * 
 * =============================================================================
 */
@Slf4j
@SpringBootConfiguration
@EnableAutoConfiguration
public class OrderApplication {

    private static String USERS_USER_ID_ORDER_URL = "users/{userId}/orders";

    public static void main(String[] args) {
        new SpringApplicationBuilder(OrderApplication.class)
                .initializers((GenericApplicationContext context) -> {
                    context.registerBean(RouterFunction.class, () -> {
                        var orderRepository = context.getBean(OrderRepository.class);
                        var orderEventPublisher = context.getBean(OrderEventPublisher.class);
                        var orderService = new OrderServiceImpl(orderRepository, orderEventPublisher);
                        return route().GET(USERS_USER_ID_ORDER_URL, request -> ok().body(orderService.findByUserId(request.pathVariable("userId")), Order.class)).build();
                    });
                })
                .build()
                .run(args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init(ApplicationReadyEvent applicationReadyEvent) {
        log.debug("OrderApplication Start... {}", applicationReadyEvent);
        var orderRepository = applicationReadyEvent.getApplicationContext().getBean(OrderRepository.class);
        orderRepository.deleteAll().subscribe();
        LongStream.rangeClosed(1, 100).forEach(u -> {
            orderRepository.saveAll(LongStream.rangeClosed(1, 100).mapToObj(p -> new Order(u, p, 100L)).collect(Collectors.toList())).subscribe();
        });
    }
}