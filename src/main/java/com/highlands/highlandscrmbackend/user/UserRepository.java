package com.highlands.highlandscrmbackend.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByCompanyIdAndEmail(UUID companyId, String email);

    boolean existsByCompanyIdAndEmail(UUID companyId, String email);
}