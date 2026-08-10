package com.deiconnect.iam.service;

import com.deiconnect.iam.entity.User;
import com.deiconnect.iam.enums.UserStatus;
import com.deiconnect.iam.repository.UserRepository;
import com.deiconnect.security.DeiUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user with email: " + email));
        return new DeiUserPrincipal(
                user.getId(),
                user.getEmployeeId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                user.getStatus() == UserStatus.ACTIVE);
    }
}
