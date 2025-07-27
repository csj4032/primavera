package com.genius.primavera.interfaces;

import com.genius.primavera.dao.UserDao;
import com.genius.primavera.domain.User;
import com.zaxxer.hikari.HikariDataSource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrimaveraController {

    private final HikariDataSource dataSource;
    private final UserDao userDao;

    @GetMapping(value = {"/", "/index"})
    public String index() throws SQLException {
        return dataSource.getCatalog();
    }

    @GetMapping(value = "users")
    public List<User> users() {
        return userDao.getUsers();
    }

    @GetMapping(value = "users/{id}")
    public User user(@PathVariable(value = "id") long id) {
        return userDao.findById(id);
    }
}