package com.deiconnect.security;

import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ForbiddenOperationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<DeiUserPrincipal> getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof DeiUserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static DeiUserPrincipal requireCurrentPrincipal() {
        return getCurrentPrincipal()
                .orElseThrow(() -> new ForbiddenOperationException("No authenticated user in context"));
    }

    public static Long getCurrentUserId() {
        return getCurrentPrincipal().map(DeiUserPrincipal::getId).orElse(null);
    }

    public static Role getCurrentRole() {
        return getCurrentPrincipal().map(DeiUserPrincipal::getRole).orElse(null);
    }

    public static boolean hasRole(Role role) {
        return role == getCurrentRole();
    }

    public static void requireOwnershipOrRole(Long ownerUserId, Role... overrideRoles) {
        DeiUserPrincipal principal = requireCurrentPrincipal();
        if (principal.getId() != null && principal.getId().equals(ownerUserId)) {
            return;
        }
        for (Role role : overrideRoles) {
            if (role == principal.getRole()) {
                return;
            }
        }
        throw new ForbiddenOperationException("You are not permitted to act on this record");
    }
}
