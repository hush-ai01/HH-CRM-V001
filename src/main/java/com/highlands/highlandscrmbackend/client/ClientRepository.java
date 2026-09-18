package com.highlands.highlandscrmbackend.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    @Query("""
            SELECT c
            FROM Client c
            WHERE c.company.id = :companyId
              AND (
                    c.owner.id = :userId
                    OR c.originatorUserId = :userId
                    OR c.createdByUserId = :userId
                    OR (
                        :managementUser = true
                        AND c.visibility =
                            com.highlands.highlandscrmbackend.client.ClientVisibility.MANAGEMENT
                    )
              )
            """)
    List<Client> findAllVisibleToUser(
            @Param("companyId") UUID companyId,
            @Param("userId") UUID userId,
            @Param("managementUser") boolean managementUser
    );

    @Query("""
            SELECT c
            FROM Client c
            WHERE c.id = :clientId
              AND c.company.id = :companyId
              AND (
                    c.owner.id = :userId
                    OR c.originatorUserId = :userId
                    OR c.createdByUserId = :userId
                    OR (
                        :managementUser = true
                        AND c.visibility =
                            com.highlands.highlandscrmbackend.client.ClientVisibility.MANAGEMENT
                    )
              )
            """)
    Optional<Client> findVisibleById(
            @Param("clientId") UUID clientId,
            @Param("companyId") UUID companyId,
            @Param("userId") UUID userId,
            @Param("managementUser") boolean managementUser
    );

    List<Client> findAllByCompanyId(UUID companyId);

    Optional<Client> findByIdAndCompanyId(
            UUID id,
            UUID companyId
    );
}