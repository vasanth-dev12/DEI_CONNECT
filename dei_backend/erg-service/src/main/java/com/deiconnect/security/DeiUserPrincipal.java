package com.deiconnect.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.deiconnect.common.enums.Role;

import lombok.Getter;

@Getter
public class DeiUserPrincipal implements UserDetails {

    private final Long id;
    private final String employeeId;
    private final String email;
    private final transient String password;
    private final Role role;
    private final boolean active;

    public DeiUserPrincipal(Long id, String employeeId, String email, String password,
                            Role role, boolean active) {
        this.id = id;
        this.employeeId = employeeId;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = active;
    }

    public static DeiUserPrincipal fromToken(Long id, String employeeId, String email, Role role) {
        return new DeiUserPrincipal(id, employeeId, email, null, role, true);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
