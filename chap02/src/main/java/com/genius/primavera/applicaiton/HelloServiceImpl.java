package com.genius.primavera.applicaiton;

import com.genius.primavera.domain.model.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HelloServiceImpl implements HelloService {

    @Override
    public List<User> getUsers() {
        return null;
    }
}
