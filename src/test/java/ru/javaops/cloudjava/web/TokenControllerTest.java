package ru.javaops.cloudjava.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import ru.javaops.cloudjava.util.JwtUtil;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaops.cloudjava.web.user.UserTestData.user;

class TokenControllerTest extends AbstractControllerTest {

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void tokenOk() throws Exception {
        String jwt = getJWT(user);
        Jwt decodedJwt = jwtDecoder.decode(jwt);
        Assertions.assertEquals(user.getEmail(), decodedJwt.getSubject());
        JwtUser jwtUser = JwtUtil.createJwtUser(decodedJwt);
        Assertions.assertEquals(user.id(), jwtUser.id());
    }

    @Test
    void tokenUnauthorized() throws Exception {
        getJWT(user.getEmail(), "dummy")
                .andExpect(status().isUnauthorized());
    }
}