package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;

    @Override
    public void run(String... args) {
        log.info("start data initialization  ... {}");
        accountRepository
                .deleteAll()
                .thenMany(Flux.just(newUserInstance(100)).flatMap(user -> accountRepository.save(user)))
                .log()
                .subscribe(null, null, () -> log.info("done initialization..."));
    }

    private User[] newUserInstance(int n) {
        User[] users = new User[n];
        for (int i = 0; i < n; i++) users[i] = new User(i, "Name" + i, LocalDateTime.now());
        return users;
    }
}