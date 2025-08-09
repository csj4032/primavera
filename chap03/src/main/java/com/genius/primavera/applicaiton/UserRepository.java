package com.genius.primavera.applicaiton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.genius.primavera.domain.User;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final List<User> users;

    public UserRepository() {
        this.users = loadUsersFromJson();
    }

    private List<User> loadUsersFromJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            ClassPathResource resource = new ClassPathResource("data/users.json");
            User[] userArray = objectMapper.readValue(resource.getInputStream(), User[].class);
            return Arrays.asList(userArray);
        } catch (IOException e) {
            System.err.println("Failed to load users.json: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<User> findAll() {
        return users;
    }

    public Optional<User> findById(Long id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }
}