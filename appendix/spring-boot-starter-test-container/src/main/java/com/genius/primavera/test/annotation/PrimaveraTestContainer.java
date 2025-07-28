package com.genius.primavera.test.annotation;

import com.genius.primavera.test.TestContainerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestContainerAutoConfiguration.class})
public @interface PrimaveraTestContainer {

    @AliasFor(annotation = SpringBootTest.class)
    SpringBootTest.WebEnvironment webEnvironment() default SpringBootTest.WebEnvironment.RANDOM_PORT;

    @AliasFor(annotation = SpringBootTest.class)
    String[] properties() default {
        "primavera.testcontainers.enabled=true",
        "primavera.testcontainers.service.enabled=true"
    };

    @AliasFor(annotation = SpringBootTest.class)
    Class<?>[] classes() default {};

    String initScript() default "sql/schema.sql";

    boolean enableInitScript() default true;

    String mariadbVersion() default "mariadb:11.4.7";

    String databaseName() default "primavera";

    String username() default "primavera";

    String password() default "primavera";

}