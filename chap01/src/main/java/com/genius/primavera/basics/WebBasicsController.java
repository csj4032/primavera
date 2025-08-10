package com.genius.primavera.basics;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequestMapping("/api/v1/basics")
@RequiredArgsConstructor
public class WebBasicsController {
    
    private final Map<Long, User> userRepository = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();
    
    @Data
    public static class User {
        private Long id;
        @NotBlank(message = "translated_text_3 translated_text_5")
        private String name;
        @Email(message = "translated_text_3 translated_text_3 translated_text_3 translated_text_5")
        private String email;
        @Size(min = 10, max = 200, message = "translated_text_3 10-200translated_text_1 translated_text_4 translated_text_3")
        private String bio;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    
    @Data
    public static class ErrorResponse {
        private final String message;
        private final int status;
        private final LocalDateTime timestamp = LocalDateTime.now();
    }
    
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userRepository.get(id);
        if (user == null) {
            throw new UserNotFoundException("translated_text_1 translated_text_2 translated_text_1 translated_text_4: " + id);
        }
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/users")
    public List<User> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        
        log.info("translated_text_1 translated_text_2 inquiry - page: {}, size: {}, name: {}", page, size, name);
        
        List<User> users = new ArrayList<>(userRepository.values());
        if (name != null) {
            users = users.stream()
                .filter(user -> user.getName().contains(name))
                .toList();
        }
        
        int start = page * size;
        int end = Math.min(start + size, users.size());
        return users.subList(start, end);
    }
    
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        user.setId(idGenerator.incrementAndGet());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.put(user.getId(), user);
        log.info("translated_text_1 translated_text_1 creation: {}", user);
        
        return ResponseEntity
            .created(URI.create("/api/v1/basics/users/" + user.getId()))
            .body(user);
    }
    
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody User updatedUser) {
        
        User existingUser = userRepository.get(id);
        if (existingUser == null) {
            throw new UserNotFoundException("translated_text_1 translated_text_2 translated_text_1 translated_text_4: " + id);
        }
        
        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setBio(updatedUser.getBio());
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        log.info("translated_text_1 translated_text_1: {}", existingUser);
        
        return ResponseEntity.ok(existingUser);
    }
    
    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        if (!userRepository.containsKey(id)) {
            throw new UserNotFoundException("translated_text_1 translated_text_2 translated_text_1 translated_text_4: " + id);
        }
        userRepository.remove(id);
        log.info("translated_text_1 deletion: {}", id);
    }
    
    @GetMapping("/headers")
    public Map<String, String> getHeaders(
            @RequestHeader("User-Agent") String userAgent,
            @RequestHeader(value = "Accept-Language", defaultValue = "ko-KR") String language) {
        
        return Map.of(
            "userAgent", userAgent,
            "language", language
        );
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
        ErrorResponse error = new ErrorResponse(e.getMessage(), 404);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("translated_text_3 translated_text_2 error translated_text_2", e);
        ErrorResponse error = new ErrorResponse("translated_text_2 error translated_text_2", 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}