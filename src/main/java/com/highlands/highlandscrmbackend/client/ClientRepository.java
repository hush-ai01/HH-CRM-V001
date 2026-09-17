package com.highlands.highlandscrmbackend.client;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    List<Client> findAllByCompanyId(UUID companyId);

    Optional<Client> findByIdAndCompanyId(
            UUID id,
            UUID companyId
    );
}