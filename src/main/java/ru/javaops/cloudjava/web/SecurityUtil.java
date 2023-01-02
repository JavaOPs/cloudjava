package ru.javaops.cloudjava.web;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static java.util.Objects.requireNonNull;

@UtilityClass
public class SecurityUtil {

    public static JwtUser safeGet() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        return (auth instanceof JwtUser) ? (JwtUser) auth : null;
    }

    public static JwtUser get() {
        return requireNonNull(safeGet(), "No authorized user found");
    }

    public static int authId() {
        return get().id();
    }
}