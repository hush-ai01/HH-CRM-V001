package com.highlands.highlandscrmbackend.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByCompanyIdAndEmail(
            UUID companyId,
            String email
    );

    Optional<User> findByIdAndCompanyId(
            UUID id,
            UUID companyId
    );

    List<User> findAllByCompanyId(
            UUID companyId
    );

    boolean existsByCompanyIdAndEmail(
            UUID companyId,
            String email
    );
}