package com.genius.primavera.application;

import com.genius.primavera.domain.model.user.User;
import com.genius.primavera.infrastructure.security.PrimaveraUserDetails;

import org.springframework.security.core.context.SecurityContextHolder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PrimaveraUtil {

    public static User getUser() {
        PrimaveraUserDetails primaveraUserDetails = (PrimaveraUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return primaveraUserDetails.getUser();
    }

    public static <T> List<T> toList( final Iterable<T> iterable) {
        return iterable instanceof List ? (List<T>) iterable 
            : StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }

    public static <T> Set<T> toSet( Iterable<T> source) {
        return source instanceof Set ? (Set<T>) source 
            : StreamSupport.stream(source.spliterator(), false).collect(Collectors.toSet());
    }
}
