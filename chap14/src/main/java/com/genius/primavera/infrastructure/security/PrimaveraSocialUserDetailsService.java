package com.genius.primavera.infrastructure.security;

import com.genius.primavera.application.user.UserService;
import com.genius.primavera.domain.model.user.User;
import com.genius.primavera.domain.model.user.UserConnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class PrimaveraSocialUserDetailsService extends PrimaveraUserDetailsService {

    private final UserService userService;

    public UsernamePasswordAuthenticationToken doAuthentication(UserConnection userConnection) {
        if (!userService.isExistUser(userConnection.getEmail())) {
            userService.signUp(userConnection);
        }
        return setAuthenticationToken((PrimaveraUserDetails) loadUserByUsername(userConnection.getEmail()));
    }

    private UsernamePasswordAuthenticationToken setAuthenticationToken(PrimaveraUserDetails primaveraUserDetails) {
        return new UsernamePasswordAuthenticationToken(primaveraUserDetails, null, primaveraUserDetails.getAuthorities());
    }
}