package com.genius.primavera.lightweight.framework;

import com.genius.primavera.lightweight.framework.testcomponents.TestController;
import com.genius.primavera.lightweight.framework.testcomponents.TestService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PrimaveraApplicationContext test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PrimaveraApplicationContextTest {

    private PrimaveraApplicationContext context;

    @BeforeEach
    void setUp() {

        context = new PrimaveraApplicationContext("com.genius.primavera.lightweight.framework.testcomponents");
    }

    @Test
    @Order(1)
    @DisplayName("ApplicationContext is successfully processing test")
    void shouldCreateApplicationContext() {
        assertNotNull(context);
    }

    @Test
    @Order(2)
    @DisplayName("@PrimaveraComponenttest should Beantest should5 test")
    void shouldRegisterComponentAsBean() {
        assertTrue(context.containsBean("testService"));
        TestService testService = context.getBean("testService");
        assertNotNull(testService);
        assertInstanceOf(TestService.class, testService);
    }

    @Test
    @Order(3)
    @DisplayName("dependency injection successfully processing test")
    void shouldInjectDependencies() {
        TestController controller = context.getBean("testController");
        assertNotNull(controller);
        assertNotNull(controller.getTestService());
        assertInstanceOf(TestService.class, controller.getTestService());
    }

    @Test
    @Order(4)
    @DisplayName("Beanshould test configuration should connection test")
    void shouldGetBeanByType() {
        TestService service = context.getBean(TestService.class);
        assertNotNull(service);
        assertInstanceOf(TestService.class, service);
    }

    @Test
    @Order(5)
    @DisplayName("file test Bean inquiry needs to be added processing test")
    void shouldThrowExceptionForNonExistentBean() {
        assertThrows(RuntimeException.class, () -> {
            context.getBean("nonExistentBean");
        });
    }

}