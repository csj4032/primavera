package com.genius.primavera.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("translated_text_3 translated_text_2 test")
public class MessageConfigTest {

    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", mariadb::getDriverClassName);
    }

    @Autowired
    private MessageSource messageSource;

    @Test
    @DisplayName("MessageSource Beantranslated_text_1 successfully translated_text_11 verification")
    void shouldCreateMessageSourceBean() {
        assertThat(messageSource).isNotNull();
    }

    @Nested
    @DisplayName("translated_text_3 translated_text_3 test")
    class MultiLanguageMessageTests {

        static Stream<Arguments> provideMultiLanguageMessages() {
            return Stream.of(

                    Arguments.of(Locale.KOREAN, "user.registration.success", "translated_text_1 translated_text_10 translated_text_14."),
                    Arguments.of(Locale.KOREAN, "label.email", "translated_text_1"),
                    Arguments.of(Locale.KOREAN, "button.save", "translated_text_2"),
                    Arguments.of(Locale.KOREAN, "com.genius.primavera.validate.nickname.message", "translated_text_4 translated_text_2 translated_text_5. (2-20translated_text_1, translated_text_2/translated_text_2/translated_text_1 translated_text_2)"),

                    Arguments.of(Locale.ENGLISH, "user.registration.success", "Registration completed successfully."),
                    Arguments.of(Locale.ENGLISH, "label.email", "Email"),
                    Arguments.of(Locale.ENGLISH, "button.save", "Save"),
                    Arguments.of(Locale.ENGLISH, "com.genius.primavera.validate.nickname.message",
                            "Invalid nickname format. (2-20 characters, Korean/English/Numbers only)"),

                    Arguments.of(Locale.JAPANESE, "user.registration.success", ""),
                    Arguments.of(Locale.JAPANESE, "label.email", ""),
                    Arguments.of(Locale.JAPANESE, "button.save", ""),
                    Arguments.of(Locale.JAPANESE, "com.genius.primavera.validate.nickname.message",
                            "(2-20///)")
            );
        }

        @ParameterizedTest
        @MethodSource("provideMultiLanguageMessages")
        @DisplayName("translated_text_4 translated_text_3 translated_text_3 translated_text_5 verification")
        void shouldLoadCorrectMessageForEachLocale(Locale locale, String messageKey, String expectedMessage) {
            String actualMessage = messageSource.getMessage(messageKey, null, locale);
            assertThat(actualMessage).isEqualTo(expectedMessage);
        }
    }

    static Stream<Arguments> provideBeanValidationMessages() {
        return Stream.of(

                Arguments.of(Locale.KOREAN, "jakarta.validation.constraints.Email.message", "translated_text_3 translated_text_1 translated_text_1 translated_text_4."),
                Arguments.of(Locale.KOREAN, "jakarta.validation.constraints.NotBlank.message", "translated_text_2 translated_text_2 translated_text_5."),
                Arguments.of(Locale.KOREAN, "jakarta.validation.constraints.NotNull.message", "translated_text_2 translated_text_2 translated_text_5."),

                Arguments.of(Locale.ENGLISH, "jakarta.validation.constraints.Email.message", "Invalid email format."),
                Arguments.of(Locale.ENGLISH, "jakarta.validation.constraints.NotBlank.message", "This field is required."),
                Arguments.of(Locale.ENGLISH, "jakarta.validation.constraints.NotNull.message", "This field must be selected."),

                Arguments.of(Locale.JAPANESE, "jakarta.validation.constraints.Email.message", ""),
                Arguments.of(Locale.JAPANESE, "jakarta.validation.constraints.NotBlank.message", ""),
                Arguments.of(Locale.JAPANESE, "jakarta.validation.constraints.NotNull.message", "")
        );
    }

    @ParameterizedTest
    @MethodSource("provideBeanValidationMessages")
    @DisplayName("Bean Validation translated_text_2 translated_text_3 translated_text_4 translated_text_6 verification")
    void shouldOverrideBeanValidationMessages(Locale locale, String messageKey, String expectedMessage) {
        String actualMessage = messageSource.getMessage(messageKey, null, locale);
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    static Stream<Arguments> provideUserStatusMessages() {
        return Stream.of(

                Arguments.of(Locale.KOREAN, "user.status.active", "translated_text_2"),
                Arguments.of(Locale.KOREAN, "user.status.inactive", "translated_text_2"),
                Arguments.of(Locale.KOREAN, "user.status.suspended", "translated_text_2"),
                Arguments.of(Locale.KOREAN, "user.status.pending", "translated_text_2 translated_text_2"),

                Arguments.of(Locale.ENGLISH, "user.status.active", "Active"),
                Arguments.of(Locale.ENGLISH, "user.status.inactive", "Inactive"),
                Arguments.of(Locale.ENGLISH, "user.status.suspended", "Suspended"),
                Arguments.of(Locale.ENGLISH, "user.status.pending", "Pending Approval"),

                Arguments.of(Locale.JAPANESE, "user.status.active", ""),
                Arguments.of(Locale.JAPANESE, "user.status.inactive", ""),
                Arguments.of(Locale.JAPANESE, "user.status.suspended", ""),
                Arguments.of(Locale.JAPANESE, "user.status.pending", "")
        );
    }

    @ParameterizedTest
    @MethodSource("provideUserStatusMessages")
    @DisplayName("translated_text_1 translated_text_2 translated_text_3 translated_text_3 translated_text_5 verification")
    void shouldTranslateUserStatusMessages(Locale locale, String messageKey, String expectedMessage) {
        String actualMessage = messageSource.getMessage(messageKey, null, locale);
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("translated_text_4 translated_text_2 translated_text_3 translated_text_2 translated_text_2 translated_text_10 translated_text_4 translated_text_2 verification")
    void shouldHandleNonExistentMessageKey() {
        String nonExistentKey = "non.existent.message.key";
        assertDoesNotThrow(() -> {
            String message = messageSource.getMessage(nonExistentKey, null, Locale.KOREAN);
            assertThat(message).isEqualTo(nonExistentKey);
        });
    }

    @Test
    @DisplayName("translated_text_5 translated_text_2 translated_text_3 translated_text_3 translated_text_13 verification")
    void shouldHandleParameterizedMessages() {
        Object[] args = {5};

        String koreanMessage = messageSource.getMessage("jakarta.validation.constraints.Min.message", args, Locale.KOREAN);
        assertThat(koreanMessage).isEqualTo("translated_text_2 5 translated_text_1translated_text_1 translated_text_3.");

        String englishMessage = messageSource.getMessage("jakarta.validation.constraints.Min.message", args, Locale.ENGLISH);
        assertThat(englishMessage).isEqualTo("Must be at least 5.");

        String japaneseMessage = messageSource.getMessage("jakarta.validation.constraints.Min.message", args, Locale.JAPANESE);
        assertThat(japaneseMessage).isEqualTo(" 5 ");
    }

    @Test
    @DisplayName("Size validation translated_text_3 translated_text_4 processing verification")
    void shouldHandleSizeValidationParameters() {
        Object[] args = {2, 10};

        String koreanMessage = messageSource.getMessage("jakarta.validation.constraints.Size.message", args, Locale.KOREAN);
        assertThat(koreanMessage).isEqualTo("translated_text_2 translated_text_3 2translated_text_1 translated_text_1 10translated_text_1 translated_text_1 translated_text_3.");

        String englishMessage = messageSource.getMessage("jakarta.validation.constraints.Size.message", args, Locale.ENGLISH);
        assertThat(englishMessage).isEqualTo("Must contain between 2 and 10 items.");

        String japaneseMessage = messageSource.getMessage("jakarta.validation.constraints.Size.message", args, Locale.JAPANESE);
        assertThat(japaneseMessage).isEqualTo(" 2  10 ");
    }

    @Test
    @DisplayName("translated_text_2 translated_text_2 translated_text_3 all translated_text_3 translated_text_5 verification")
    void shouldProvideRoleMessagesInAllLanguages() {

        assertThat(messageSource.getMessage("role.administrator", null, Locale.KOREAN)).isEqualTo("translated_text_1");
        assertThat(messageSource.getMessage("role.manager", null, Locale.KOREAN)).isEqualTo("translated_text_3");
        assertThat(messageSource.getMessage("role.user", null, Locale.KOREAN)).isEqualTo("translated_text_2 translated_text_1");

        assertThat(messageSource.getMessage("role.administrator", null, Locale.ENGLISH)).isEqualTo("Administrator");
        assertThat(messageSource.getMessage("role.manager", null, Locale.ENGLISH)).isEqualTo("Manager");
        assertThat(messageSource.getMessage("role.user", null, Locale.ENGLISH)).isEqualTo("User");

        assertThat(messageSource.getMessage("role.administrator", null, Locale.JAPANESE)).isEqualTo("");
        assertThat(messageSource.getMessage("role.manager", null, Locale.JAPANESE)).isEqualTo("");
        assertThat(messageSource.getMessage("role.user", null, Locale.JAPANESE)).isEqualTo("");
    }

    @Test
    @DisplayName("translated_text_2 translated_text_1 translated_text_3 all translated_text_3 translated_text_5 verification")
    void shouldProvideErrorPageMessagesInAllLanguages() {

        assertThat(messageSource.getMessage("error.400.message", null, Locale.KOREAN)).isEqualTo("translated_text_3 translated_text_5.");
        assertThat(messageSource.getMessage("error.404.message", null, Locale.KOREAN)).isEqualTo("translated_text_1 translated_text_2 translated_text_1 translated_text_4.");
        assertThat(messageSource.getMessage("error.500.message", null, Locale.KOREAN)).isEqualTo("translated_text_2 translated_text_2 translated_text_6 translated_text_6.");

        assertThat(messageSource.getMessage("error.400.message", null, Locale.ENGLISH)).isEqualTo("Bad Request.");
        assertThat(messageSource.getMessage("error.404.message", null, Locale.ENGLISH)).isEqualTo("Page Not Found.");
        assertThat(messageSource.getMessage("error.500.message", null, Locale.ENGLISH)).isEqualTo("Internal Server Error.");

        assertThat(messageSource.getMessage("error.400.message", null, Locale.JAPANESE)).isEqualTo("");
        assertThat(messageSource.getMessage("error.404.message", null, Locale.JAPANESE)).isEqualTo("");
        assertThat(messageSource.getMessage("error.500.message", null, Locale.JAPANESE)).isEqualTo("");
    }
}