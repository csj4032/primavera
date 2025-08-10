package com.genius.primavera.interfaces;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageSource messageSource;

    @Autowired
    public MessageController(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentLocaleInfo() {
        Locale currentLocale = LocaleContextHolder.getLocale();
        
        Map<String, Object> response = new HashMap<>();
        response.put("currentLocale", currentLocale.toString());
        response.put("language", currentLocale.getLanguage());
        response.put("country", currentLocale.getCountry());
        response.put("displayName", currentLocale.getDisplayName());

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

    @GetMapping("/all-languages")
    public ResponseEntity<Map<String, Object>> getMessageInAllLanguages(@RequestParam String key) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> messages = new HashMap<>();

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

    @GetMapping("/validation-messages")
    public ResponseEntity<Map<String, String>> getValidationMessages() {
        Locale currentLocale = LocaleContextHolder.getLocale();
        
        Map<String, String> messages = new HashMap<>();

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