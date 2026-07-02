package com.flowpay.routing.monitoring.distribution.infrastructure.security;

import com.flowpay.routing.monitoring.distribution.infrastructure.persistence.repository.AppUserJpaRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Loads login accounts (and their role) from the app_user table seeded by Flyway. */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final AppUserJpaRepository users;

    public DatabaseUserDetailsService(AppUserJpaRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return users.findByUsername(username)
                .map(user -> User.withUsername(user.getUsername())
                        .password(user.getPasswordHash())
                        .roles(user.getRole())
                        .disabled(!user.isEnabled())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user: " + username));
    }
}
