package com.genius.primavera.interfaces;

import com.genius.primavera.application.HelloService;
import com.genius.primavera.application.WorldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HelloController - Spring Bean 등록 방식 학습 예제
 * <p>
 * 이 클래스는 두 가지 Bean 등록 방식을 보여줍니다:
 * 1. 어노테이션 기반: @RestController (컴포넌트 스캔에 의한 자동 등록)
 * 2. 프로그래매틱: SpringBootStarterApplication에서 수동 등록
 * <p>
 * 주의: 두 방식을 동시에 사용하면 Bean 중복 등록으로 인한 충돌이 발생할 수 있습니다.
 * 충돌 발생 시 해결 방법:
 * - SpringBootStarterApplication의 HelloController 등록 부분 주석 처리, 또는
 * - 이 클래스의 @RestController 어노테이션 제거
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class HelloController {

    private final HelloService helloService;
    private final WorldService worldService;

    @GetMapping(value = "/hello")
    public String hello() {
        return helloService.hello() + " " + worldService.world();
    }

    public String world() {
        return worldService.world();
    }
}