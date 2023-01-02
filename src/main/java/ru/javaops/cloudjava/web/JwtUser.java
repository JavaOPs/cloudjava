package ru.javaops.cloudjava.web;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.javaops.cloudjava.HasIdAndEmail;

import java.util.Collection;

public class JwtUser extends JwtAuthenticationToken implements HasIdAndEmail {
    @Getter
    @Setter
    private Integer id;

    public JwtUser(Jwt jwt, Collection<? extends GrantedAuthority> authorities, int id) {
        super(jwt, authorities);
        this.id = id;
    }

    @Override
    public String getEmail() {
        return getName();
    }

    @Override
    public String toString() {
        return "JwtUser:" + id + '[' + getName() + ']';
    }
}