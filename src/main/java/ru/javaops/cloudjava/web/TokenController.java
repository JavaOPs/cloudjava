/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.javaops.cloudjava.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.javaops.cloudjava.util.JwtUtil;

import java.time.Instant;

/**
 * A controller for the token resource.
 *
 * @author Josh Cummings
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class TokenController {

    @Value("${jwt.expiry}")
    long expiry;

    private final JwtEncoder encoder;

    @PostMapping("/token")
    public String token(@AuthenticationPrincipal AuthUser authUser) {
        log.debug("create JWT for '{}'", authUser);
        Instant now = Instant.now();
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry));
        JwtUtil.addUserDetails(claimsBuilder, authUser);
        return encoder.encode(JwtEncoderParameters.from(claimsBuilder.build())).getTokenValue();
    }
}
