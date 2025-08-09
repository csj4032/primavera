package com.genius.primavera.applicaiton;

import com.genius.primavera.domain.User;
import com.genius.primavera.infrastructure.aspect.PrimaveraLogging;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HelloServiceImpl implements HelloService {

    private final UserRepository userRepository;

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    @PrimaveraLogging(type = "Service")
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new OopsException("User not found with id: " + id));
    }
}