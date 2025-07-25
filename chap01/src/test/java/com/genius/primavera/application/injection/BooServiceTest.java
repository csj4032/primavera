package com.genius.primavera.application.injection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BooServiceTest {

    @Mock
    private FooService fooService;

    @InjectMocks
    private BooServiceImpl booService;

    @BeforeEach
    void setUp() {
        // FooService의 foo() 메서드가 호출되면 "mock-foo"를 반환하도록 설정
        when(fooService.foo()).thenReturn("mock-foo");
    }

    @Test
    @DisplayName("boo() 메서드는 FooService.foo()를 호출하고 'boo'를 반환해야 한다")
    void booMethodShouldCallFooAndReturnBoo() {
        // given
        // 이미 setUp()에서 fooService mock 설정 완료

        // when
        String result = booService.boo();

        // then
        // 1. FooService.foo() 메서드가 정확히 한 번 호출되었는지 확인
        verify(fooService, times(1)).foo();

        // 2. "boo" 문자열이 반환되는지 확인
        assertEquals("boo", result);
    }

    @Test
    @DisplayName("순환 참조 문제가 발생하지 않아야 한다")
    void circularDependencyShouldBeHandled() {
        // given
        // Mock 객체로 순환 참조 문제 해결

        // when
        String result = booService.boo();

        // then
        // 순환 참조로 인한 StackOverflowError가 발생하지 않고 정상 실행
        assertEquals("boo", result);
    }

    @Test
    @DisplayName("BooService가 FooService에 의존하고 있음을 검증한다")
    void shouldDependOnFooService() {
        // given
        // Mock 객체 설정 완료

        // when
        booService.boo();

        // then
        // FooService와의 상호작용 검증
        verify(fooService).foo();

        // 상호작용 이�� 더 이상의 호출이 없어야 함
        verifyNoMoreInteractions(fooService);
    }
}