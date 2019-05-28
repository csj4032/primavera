package com.genius.primavera.application;

import com.genius.primavera.domain.model.user.User;
import com.genius.primavera.infrastructure.security.PrimaveraUserDetails;

import org.springframework.security.core.context.SecurityContextHolder;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PrimaveraUtil {

    public static User getUser() {
        PrimaveraUserDetails primaveraUserDetails = (PrimaveraUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return primaveraUserDetails.getUser();
    }
}
