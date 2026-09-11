package com.highlands.highlandscrmbackend.auth;

import com.highlands.highlandscrmbackend.common.exception.ResourceNotFoundException;
import com.highlands.highlandscrmbackend.company.CompanyRepository;
import com.highlands.highlandscrmbackend.user.User;
import com.highlands.highlandscrmbackend.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.highlands.highlandscrmbackend.role.Role;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        if (!companyRepository.existsById(request.companyId())) {
            throw new ResourceNotFoundException(
                    "Company with id '" + request.companyId() + "' not found"
            );
        }

        User user = userRepository
                .findByCompanyIdAndEmail(
                        request.companyId(),
                        request.email()
                )
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or password"
                        )
                );

        if (!user.isActive()) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        boolean passwordMatches = passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        String accessToken = jwtService.generateToken(
                user.getId(),
                user.getCompany().getId(),
                user.getEmail(),
                roles
        );

        return new LoginResponse(
                accessToken,
                "Bearer",
                1800,
                user.getId(),
                user.getCompany().getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}