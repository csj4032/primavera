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
    @DisplayName("ApplicationContext is successfully translated_text_11 test")
    void shouldCreateApplicationContext() {
        assertNotNull(context);
    }

    @Test
    @Order(2)
    @DisplayName("@PrimaveraComponenttranslated_text_1 translated_text_2 translated_text_1 Beantranslated_text_2 translated_text_15 test")
    void shouldRegisterComponentAsBean() {
        assertTrue(context.containsBean("testService"));
        TestService testService = context.getBean("testService");
        assertNotNull(testService);
        assertInstanceOf(TestService.class, testService);
    }

    @Test
    @Order(3)
    @DisplayName("dependency injection successfully translated_text_5 test")
    void shouldInjectDependencies() {
        TestController controller = context.getBean("testController");
        assertNotNull(controller);
        assertNotNull(controller.getTestService());
        assertInstanceOf(TestService.class, controller.getTestService());
    }

    @Test
    @Order(4)
    @DisplayName("Beantranslated_text_1 translated_text_2 translated_text_8 translated_text_1 translated_text_3 test")
    void shouldGetBeanByType() {
        TestService service = context.getBean(TestService.class);
        assertNotNull(service);
        assertInstanceOf(TestService.class, service);
    }

    @Test
    @Order(5)
    @DisplayName("translated_text_4 translated_text_2 Bean inquiry translated_text_1 translated_text_1 translated_text_5 test")
    void shouldThrowExceptionForNonExistentBean() {
        assertThrows(RuntimeException.class, () -> {
            context.getBean("nonExistentBean");
        });
    }

}