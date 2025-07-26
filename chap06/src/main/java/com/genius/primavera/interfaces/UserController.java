package com.genius.primavera.interfaces;

import com.genius.primavera.application.UserService;
import com.genius.primavera.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/users")
public class UserController {

    private final UserService userService;

    @GetMapping(value = "/{id}")
    public long getUserById(@PathVariable(value = "id") long id) {
        return id;
    }

    @PostMapping(value = "/save")
    public ResponseEntity<User> save(@RequestBody @Validated(User.SaveGroup.class) User user, BindingResult bindingResult) {
        log.info("User Save : {}", bindingResult);
        if (!bindingResult.hasErrors()) {
            userService.save(user);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "/update")
    public ResponseEntity<User> update(@RequestBody @Validated(User.UpdateGroup.class) User user, BindingResult bindingResult) {
        log.info("User Update : {}", bindingResult);
        if (!bindingResult.hasErrors()) {
            userService.update(user);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
    }
}