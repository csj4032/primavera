package com.genius.primavera.interfaces;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 국제화 메시지 기능을 테스트하고 시연하는 컨트롤러입니다.
 * 
 * 주요 기능:
 * - 현재 로케일에 따른 메시지 반환
 * - 언어 변경 기능 테스트
 * - AJAX를 통한 동적 메시지 조회
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageSource messageSource;

    @Autowired
    public MessageController(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 현재 로케일 정보와 샘플 메시지를 반환합니다.
     * 
     * @return 현재 로케일과 다국어 메시지 정보
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentLocaleInfo() {
        Locale currentLocale = LocaleContextHolder.getLocale();
        
        Map<String, Object> response = new HashMap<>();
        response.put("currentLocale", currentLocale.toString());
        response.put("language", currentLocale.getLanguage());
        response.put("country", currentLocale.getCountry());
        response.put("displayName", currentLocale.getDisplayName());
        
        // 샘플 메시지들
        Map<String, String> messages = new HashMap<>();
        messages.put("userRegistrationSuccess", messageSource.getMessage("user.registration.success", null, currentLocale));
        messages.put("userUpdateSuccess", messageSource.getMessage("user.update.success", null, currentLocale));
        messages.put("buttonSave", messageSource.getMessage("button.save", null, currentLocale));
        messages.put("buttonCancel", messageSource.getMessage("button.cancel", null, currentLocale));
        messages.put("labelEmail", messageSource.getMessage("label.email", null, currentLocale));
        messages.put("labelPassword", messageSource.getMessage("label.password", null, currentLocale));
        
        response.put("messages", messages);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 메시지 키에 대한 현재 로케일의 메시지를 반환합니다.
     * 
     * @param key 메시지 키
     * @return 현재 로케일에 해당하는 메시지
     */
    @GetMapping("/get")
    public ResponseEntity<Map<String, String>> getMessage(@RequestParam String key) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        
        Map<String, String> response = new HashMap<>();
        try {
            String message = messageSource.getMessage(key, null, currentLocale);
            response.put("key", key);
            response.put("message", message);
            response.put("locale", currentLocale.toString());
            response.put("status", "success");
        } catch (Exception e) {
            response.put("key", key);
            response.put("message", "Message not found for key: " + key);
            response.put("locale", currentLocale.toString());
            response.put("status", "error");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 파라미터가 있는 메시지를 반환합니다.
     * 
     * @param key 메시지 키
     * @param args 메시지 파라미터들 (쉼표로 구분)
     * @return 파라미터가 적용된 메시지
     */
    @GetMapping("/get-with-params")
    public ResponseEntity<Map<String, String>> getMessageWithParams(
            @RequestParam String key, 
            @RequestParam(required = false) String args) {
        
        Locale currentLocale = LocaleContextHolder.getLocale();
        
        Map<String, String> response = new HashMap<>();
        try {
            Object[] messageArgs = null;
            if (args != null && !args.trim().isEmpty()) {
                messageArgs = args.split(",");
            }
            
            String message = messageSource.getMessage(key, messageArgs, currentLocale);
            response.put("key", key);
            response.put("message", message);
            response.put("args", args != null ? args : "");
            response.put("locale", currentLocale.toString());
            response.put("status", "success");
        } catch (Exception e) {
            response.put("key", key);
            response.put("message", "Message not found for key: " + key);
            response.put("args", args != null ? args : "");
            response.put("locale", currentLocale.toString());
            response.put("status", "error");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 지원되는 모든 언어의 특정 메시지를 반환합니다.
     * 
     * @param key 메시지 키
     * @return 모든 지원 언어의 메시지
     */
    @GetMapping("/all-languages")
    public ResponseEntity<Map<String, Object>> getMessageInAllLanguages(@RequestParam String key) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> messages = new HashMap<>();
        
        // 지원하는 로케일들
        Locale[] supportedLocales = {
            Locale.KOREAN,
            Locale.ENGLISH,
            Locale.JAPANESE
        };
        
        for (Locale locale : supportedLocales) {
            try {
                String message = messageSource.getMessage(key, null, locale);
                messages.put(locale.toString(), message);
            } catch (Exception e) {
                messages.put(locale.toString(), "Message not found");
            }
        }
        
        response.put("key", key);
        response.put("messages", messages);
        response.put("supportedLocales", new String[]{"ko", "en", "ja"});
        
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 검증 관련 메시지들을 모두 반환합니다.
     * 
     * @return 현재 로케일의 모든 검증 메시지
     */
    @GetMapping("/validation-messages")
    public ResponseEntity<Map<String, String>> getValidationMessages() {
        Locale currentLocale = LocaleContextHolder.getLocale();
        
        Map<String, String> messages = new HashMap<>();
        
        // 검증 관련 메시지 키들
        String[] validationKeys = {
            "com.genius.primavera.validate.nickname.message",
            "com.genius.primavera.validate.password.match.message",
            "user.validation.password.pattern",
            "jakarta.validation.constraints.NotBlank.message",
            "jakarta.validation.constraints.Email.message",
            "jakarta.validation.constraints.Pattern.message"
        };
        
        for (String key : validationKeys) {
            try {
                String message = messageSource.getMessage(key, null, currentLocale);
                messages.put(key, message);
            } catch (Exception e) {
                messages.put(key, "Message not found");
            }
        }
        
        messages.put("currentLocale", currentLocale.toString());
        
        return ResponseEntity.ok(messages);
    }
}