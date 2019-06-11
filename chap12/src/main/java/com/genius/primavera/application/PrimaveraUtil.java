package com.genius.primavera.application;

import com.genius.primavera.domain.model.user.User;
import com.genius.primavera.infrastructure.security.PrimaveraUserDetails;

import org.springframework.security.core.context.SecurityContextHolder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PrimaveraUtil {

    public static User getUser() {
        PrimaveraUserDetails primaveraUserDetails = (PrimaveraUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return primaveraUserDetails.getUser();
    }

    public static <T> List<T> toList( final Iterable<T> iterable) {
        if (iterable instanceof List) {
            return (List<T>) iterable;
        } else {
            List<T> list = new ArrayList<>();
            for (T item : iterable) {
                list.add(item);
            }
            return list;
        }
    }

    public static <T> Set<T> toSet( Iterable<T> source) {
        if (source instanceof Set) {
            return (Set<T>) source;
        } else {
            Set<T> set = new HashSet<>();

            for (T item : source) {
                set.add(item);
            }
            return set;
        }
    }
}
