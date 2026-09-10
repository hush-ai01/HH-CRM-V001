package com.highlands.highlandscrmbackend.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    List<Role> findAllByCompanyId(UUID companyId);

    Optional<Role> findByCompanyIdAndName(UUID companyId, String name);

    boolean existsByCompanyIdAndName(UUID companyId, String name);
}