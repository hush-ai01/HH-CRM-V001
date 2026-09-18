package com.highlands.highlandscrmbackend.security;

import com.highlands.highlandscrmbackend.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthorizationService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public AuthorizationService(
            UserRepository userRepository,
            CurrentUserService currentUserService
    ) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    public boolean hasPermission(String permissionCode) {

        UUID userId =
                currentUserService.getCurrentUserId();

        UUID companyId =
                currentUserService.getCurrentCompanyId();

        return userRepository.hasPermission(
                userId,
                companyId,
                permissionCode
        );
    }

    public void requirePermission(String permissionCode) {

        if (!hasPermission(permissionCode)) {
            throw new ForbiddenException(
                    "You do not have permission to perform this action"
            );
        }
    }
}