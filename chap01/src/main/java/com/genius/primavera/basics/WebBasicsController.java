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
        @NotBlank(message = "이름은 필수입니다")
        private String name;
        @Email(message = "유효한 이메일 주소를 입력하세요")
        private String email;
        @Size(min = 10, max = 200, message = "소개는 10-200자 사이여야 합니다")
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
            throw new UserNotFoundException("사용자를 찾을 수 없습니다: " + id);
        }
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/users")
    public List<User> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        
        log.info("사용자 목록 조회 - page: {}, size: {}, name: {}", page, size, name);
        
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
        log.info("새 사용자 생성: {}", user);
        
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
            throw new UserNotFoundException("사용자를 찾을 수 없습니다: " + id);
        }
        
        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setBio(updatedUser.getBio());
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        log.info("사용자 수정: {}", existingUser);
        
        return ResponseEntity.ok(existingUser);
    }
    
    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        if (!userRepository.containsKey(id)) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다: " + id);
        }
        userRepository.remove(id);
        log.info("사용자 삭제: {}", id);
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
        log.error("예상치 못한 오류 발생", e);
        ErrorResponse error = new ErrorResponse("서버 오류가 발생했습니다", 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}