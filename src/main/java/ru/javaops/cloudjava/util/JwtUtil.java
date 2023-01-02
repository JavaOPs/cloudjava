package ru.javaops.cloudjava.util;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import ru.javaops.cloudjava.model.User;
import ru.javaops.cloudjava.web.AuthUser;

import java.util.stream.Collectors;

public class JwtUtil {

    public static void addUserDetails(JwtClaimsSet.Builder builder, AuthUser authUser) {
        User user = authUser.getUser();
        String roles = user.getRoles().stream()
                .map(Enum::name).collect(Collectors.joining(" "));
        builder.subject(user.getEmail())
                .claim("id", user.id())
                .claim("roles", roles);
    }
}
