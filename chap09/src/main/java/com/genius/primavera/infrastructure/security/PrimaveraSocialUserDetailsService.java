package com.genius.primavera.infrastructure.security;

import com.genius.primavera.application.UserService;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserConnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class PrimaveraSocialUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    public UsernamePasswordAuthenticationToken doAuthentication(UserConnection userConnection) {
        User user = userService.findByEmail(userConnection.getEmail());
        if (user == null) {
            return setAuthenticationToken(userService.signUp(userConnection));
        }
        return setAuthenticationToken(user);
    }

    private UsernamePasswordAuthenticationToken setAuthenticationToken(User user) {
        List<? extends GrantedAuthority> authorities = user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getType().toString())).collect(toList());
        return new UsernamePasswordAuthenticationToken(new PrimaveraUserDetails(user), null, authorities);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
