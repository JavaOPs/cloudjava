package ru.javaops.cloudjava.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import ru.javaops.cloudjava.model.Role;
import ru.javaops.cloudjava.model.User;
import ru.javaops.cloudjava.web.AuthUser;
import ru.javaops.cloudjava.web.JwtUser;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class JwtUtil {

    public static void addUserDetails(JwtClaimsSet.Builder builder, AuthUser authUser) {
        User user = authUser.getUser();
        String roles = user.getRoles().stream()
                .map(Enum::name).collect(Collectors.joining(" "));
        builder.subject(user.getEmail())
                .claim("id", user.id())
                .claim("roles", roles);
    }

    public static JwtUser createJwtUser(Jwt jwt) {
        log.debug("create JwtUser for '{}'", jwt.getSubject());
        int id = ((Long) jwt.getClaims().get("id")).intValue();
        String rawRoles = (String) jwt.getClaims().get("roles");
        Set<Role> roles = Arrays.stream(rawRoles.split(" "))
                .map(Role::valueOf)
                .collect(Collectors.toSet());
        return new JwtUser(jwt, roles, id);
    }
}
