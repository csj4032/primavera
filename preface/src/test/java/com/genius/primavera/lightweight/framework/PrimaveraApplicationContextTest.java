package com.genius.primavera.lightweight.framework;

import com.genius.primavera.lightweight.framework.testcomponents.TestController;
import com.genius.primavera.lightweight.framework.testcomponents.TestService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("PrimaveraApplicationContext 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PrimaveraApplicationContextTest {

    private PrimaveraApplicationContext context;

    @BeforeEach
    void setUp() {
        // Scan dedicated test components package to avoid other test classes
        context = new PrimaveraApplicationContext("com.genius.primavera.lightweight.framework.testcomponents");
    }

    @Test
    @Order(1)
    @DisplayName("ApplicationContext가 정상적으로 생성되는지 테스트")
    void shouldCreateApplicationContext() {
        assertNotNull(context);
    }

    @Test
    @Order(2)
    @DisplayName("@PrimaveraComponent가 붙은 클래스가 Bean으로 등록되는지 테스트")
    void shouldRegisterComponentAsBean() {
        assertTrue(context.containsBean("testService"));
        TestService testService = context.getBean("testService");
        assertNotNull(testService);
        assertInstanceOf(TestService.class, testService);
    }


    @Test
    @Order(3)
    @DisplayName("의존성 주입이 정상적으로 동작하는지 테스트")
    void shouldInjectDependencies() {
        TestController controller = context.getBean("testController");
        assertNotNull(controller);
        assertNotNull(controller.getTestService());
        assertInstanceOf(TestService.class, controller.getTestService());
    }

    @Test
    @Order(4)
    @DisplayName("Bean을 타입으로 조회할 수 있는지 테스트")
    void shouldGetBeanByType() {
        TestService service = context.getBean(TestService.class);
        assertNotNull(service);
        assertInstanceOf(TestService.class, service);
    }

    @Test
    @Order(5)
    @DisplayName("존재하지 않는 Bean 조회 시 예외가 발생하는지 테스트")
    void shouldThrowExceptionForNonExistentBean() {
        assertThrows(RuntimeException.class, () -> {
            context.getBean("nonExistentBean");
        });
    }

}