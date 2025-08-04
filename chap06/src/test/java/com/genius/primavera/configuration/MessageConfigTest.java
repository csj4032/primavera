package com.genius.primavera.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;

import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * 국제화 설정과 메시지 소스 기능을 테스트하는 클래스입니다.
 * <p>
 * 테스트 범위:
 * - MessageSource Bean 생성 확인
 * - 다국어 메시지 로딩 테스트
 * - 로케일별 메시지 정확성 검증
 * - 존재하지 않는 메시지 키 처리
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("국제화 설정 테스트")
class MessageConfigTest {

    @Autowired
    private MessageSource messageSource;

    @Test
    @DisplayName("MessageSource Bean이 정상적으로 생성되는지 확인")
    void shouldCreateMessageSourceBean() {
        assertThat(messageSource).isNotNull();
    }

    static Stream<Arguments> provideMultiLanguageMessages() {
        return Stream.of(
                // 한국어
                Arguments.of(Locale.KOREAN, "user.registration.success", "회원가입이 성공적으로 완료되었습니다."),
                Arguments.of(Locale.KOREAN, "label.email", "이메일"),
                Arguments.of(Locale.KOREAN, "button.save", "저장"),
                Arguments.of(Locale.KOREAN, "com.genius.primavera.validate.nickname.message", "올바르지 않은 별명입니다. (2-20자, 한글/영문/숫자만 허용)"),

                // 영어
                Arguments.of(Locale.ENGLISH, "user.registration.success", "Registration completed successfully."),
                Arguments.of(Locale.ENGLISH, "label.email", "Email"),
                Arguments.of(Locale.ENGLISH, "button.save", "Save"),
                Arguments.of(Locale.ENGLISH, "com.genius.primavera.validate.nickname.message",
                        "Invalid nickname format. (2-20 characters, Korean/English/Numbers only)"),

                // 일본어
                Arguments.of(Locale.JAPANESE, "user.registration.success", "会員登録が正常に完了しました。"),
                Arguments.of(Locale.JAPANESE, "label.email", "メールアドレス"),
                Arguments.of(Locale.JAPANESE, "button.save", "保存"),
                Arguments.of(Locale.JAPANESE, "com.genius.primavera.validate.nickname.message",
                        "ニックネームの形式が正しくありません。(2-20文字、ひらがな/カタカナ/漢字/英数字のみ許可)")
        );
    }

    @ParameterizedTest
    @MethodSource("provideMultiLanguageMessages")
    @DisplayName("로케일별 메시지가 정확히 로딩되는지 확인")
    void shouldLoadCorrectMessageForEachLocale(Locale locale, String messageKey, String expectedMessage) {
        String actualMessage = messageSource.getMessage(messageKey, null, locale);
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    static Stream<Arguments> provideBeanValidationMessages() {
        return Stream.of(
                // 한국어
                Arguments.of(Locale.KOREAN, "jakarta.validation.constraints.Email.message", "올바른 이메일 형식이 아닙니다."),
                Arguments.of(Locale.KOREAN, "jakarta.validation.constraints.NotBlank.message", "필수 입력 항목입니다."),
                Arguments.of(Locale.KOREAN, "jakarta.validation.constraints.NotNull.message", "필수 선택 항목입니다."),

                // 영어
                Arguments.of(Locale.ENGLISH, "jakarta.validation.constraints.Email.message", "Invalid email format."),
                Arguments.of(Locale.ENGLISH, "jakarta.validation.constraints.NotBlank.message", "This field is required."),
                Arguments.of(Locale.ENGLISH, "jakarta.validation.constraints.NotNull.message", "This field must be selected."),

                // 일본어
                Arguments.of(Locale.JAPANESE, "jakarta.validation.constraints.Email.message", "正しいメールアドレスの形式ではありません。"),
                Arguments.of(Locale.JAPANESE, "jakarta.validation.constraints.NotBlank.message", "この項目は必須です。"),
                Arguments.of(Locale.JAPANESE, "jakarta.validation.constraints.NotNull.message", "この項目を選択してください。")
        );
    }

    @ParameterizedTest
    @MethodSource("provideBeanValidationMessages")
    @DisplayName("Bean Validation 표준 메시지가 올바르게 재정의되는지 확인")
    void shouldOverrideBeanValidationMessages(Locale locale, String messageKey, String expectedMessage) {
        String actualMessage = messageSource.getMessage(messageKey, null, locale);
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    static Stream<Arguments> provideUserStatusMessages() {
        return Stream.of(
                // 한국어
                Arguments.of(Locale.KOREAN, "user.status.active", "활성"),
                Arguments.of(Locale.KOREAN, "user.status.inactive", "비활성"),
                Arguments.of(Locale.KOREAN, "user.status.suspended", "정지"),
                Arguments.of(Locale.KOREAN, "user.status.pending", "승인 대기"),

                // 영어
                Arguments.of(Locale.ENGLISH, "user.status.active", "Active"),
                Arguments.of(Locale.ENGLISH, "user.status.inactive", "Inactive"),
                Arguments.of(Locale.ENGLISH, "user.status.suspended", "Suspended"),
                Arguments.of(Locale.ENGLISH, "user.status.pending", "Pending Approval"),

                // 일본어
                Arguments.of(Locale.JAPANESE, "user.status.active", "アクティブ"),
                Arguments.of(Locale.JAPANESE, "user.status.inactive", "非アクティブ"),
                Arguments.of(Locale.JAPANESE, "user.status.suspended", "停止"),
                Arguments.of(Locale.JAPANESE, "user.status.pending", "承認待ち")
        );
    }

    @ParameterizedTest
    @MethodSource("provideUserStatusMessages")
    @DisplayName("사용자 상태 메시지가 정확히 번역되는지 확인")
    void shouldTranslateUserStatusMessages(Locale locale, String messageKey, String expectedMessage) {
        String actualMessage = messageSource.getMessage(messageKey, null, locale);
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("존재하지 않는 메시지 키에 대해 예외가 발생하지 않는지 확인")
    void shouldHandleNonExistentMessageKey() {
        String nonExistentKey = "non.existent.message.key";
        assertDoesNotThrow(() -> {
            String message = messageSource.getMessage(nonExistentKey, null, Locale.KOREAN);
            assertThat(message).isEqualTo(nonExistentKey);
        });
    }

    @Test
    @DisplayName("파라미터가 있는 메시지가 정확히 처리되는지 확인")
    void shouldHandleParameterizedMessages() {
        Object[] args = {5};

        String koreanMessage = messageSource.getMessage("jakarta.validation.constraints.Min.message", args, Locale.KOREAN);
        assertThat(koreanMessage).isEqualTo("최소 5 이상이어야 합니다.");

        String englishMessage = messageSource.getMessage("jakarta.validation.constraints.Min.message", args, Locale.ENGLISH);
        assertThat(englishMessage).isEqualTo("Must be at least 5.");

        String japaneseMessage = messageSource.getMessage("jakarta.validation.constraints.Min.message", args, Locale.JAPANESE);
        assertThat(japaneseMessage).isEqualTo("最小 5 以上である必要があります。");
    }

    @Test
    @DisplayName("Size validation 메시지 파라미터 처리 확인")
    void shouldHandleSizeValidationParameters() {
        Object[] args = {2, 10};

        String koreanMessage = messageSource.getMessage("jakarta.validation.constraints.Size.message", args, Locale.KOREAN);
        assertThat(koreanMessage).isEqualTo("항목 개수가 2개 이상 10개 이하여야 합니다.");

        String englishMessage = messageSource.getMessage("jakarta.validation.constraints.Size.message", args, Locale.ENGLISH);
        assertThat(englishMessage).isEqualTo("Must contain between 2 and 10 items.");

        String japaneseMessage = messageSource.getMessage("jakarta.validation.constraints.Size.message", args, Locale.JAPANESE);
        assertThat(japaneseMessage).isEqualTo("項目数は 2 個以上 10 個以下である必要があります。");
    }

    @Test
    @DisplayName("권한 관련 메시지가 모든 언어로 제공되는지 확인")
    void shouldProvideRoleMessagesInAllLanguages() {
        // 한국어
        assertThat(messageSource.getMessage("role.administrator", null, Locale.KOREAN)).isEqualTo("관리자");
        assertThat(messageSource.getMessage("role.manager", null, Locale.KOREAN)).isEqualTo("매니저");
        assertThat(messageSource.getMessage("role.user", null, Locale.KOREAN)).isEqualTo("일반 사용자");

        // 영어
        assertThat(messageSource.getMessage("role.administrator", null, Locale.ENGLISH)).isEqualTo("Administrator");
        assertThat(messageSource.getMessage("role.manager", null, Locale.ENGLISH)).isEqualTo("Manager");
        assertThat(messageSource.getMessage("role.user", null, Locale.ENGLISH)).isEqualTo("User");

        // 일본어
        assertThat(messageSource.getMessage("role.administrator", null, Locale.JAPANESE)).isEqualTo("管理者");
        assertThat(messageSource.getMessage("role.manager", null, Locale.JAPANESE)).isEqualTo("マネージャー");
        assertThat(messageSource.getMessage("role.user", null, Locale.JAPANESE)).isEqualTo("一般ユーザー");
    }

    @Test
    @DisplayName("에러 페이지 메시지가 모든 언어로 제공되는지 확인")
    void shouldProvideErrorPageMessagesInAllLanguages() {
        // 한국어
        assertThat(messageSource.getMessage("error.400.message", null, Locale.KOREAN)).isEqualTo("잘못된 요청입니다.");
        assertThat(messageSource.getMessage("error.404.message", null, Locale.KOREAN)).isEqualTo("페이지를 찾을 수 없습니다.");
        assertThat(messageSource.getMessage("error.500.message", null, Locale.KOREAN)).isEqualTo("서버 내부 오류가 발생했습니다.");

        // 영어
        assertThat(messageSource.getMessage("error.400.message", null, Locale.ENGLISH)).isEqualTo("Bad Request.");
        assertThat(messageSource.getMessage("error.404.message", null, Locale.ENGLISH)).isEqualTo("Page Not Found.");
        assertThat(messageSource.getMessage("error.500.message", null, Locale.ENGLISH)).isEqualTo("Internal Server Error.");

        // 일본어
        assertThat(messageSource.getMessage("error.400.message", null, Locale.JAPANESE)).isEqualTo("リクエストが正しくありません。");
        assertThat(messageSource.getMessage("error.404.message", null, Locale.JAPANESE)).isEqualTo("ページが見つかりません。");
        assertThat(messageSource.getMessage("error.500.message", null, Locale.JAPANESE)).isEqualTo("サーバー内部エラーが発生しました。");
    }
}