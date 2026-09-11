package com.highlands.highlandscrmbackend.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldStoreAndRetrieveCompanyId() {
        UUID companyId = UUID.randomUUID();

        TenantContext.setCompanyId(companyId);

        assertEquals(companyId, TenantContext.getCompanyId());
    }

    @Test
    void shouldReturnNullWhenNoCompanyContextExists() {
        assertNull(TenantContext.getCompanyId());
    }

    @Test
    void shouldRequireCompanyId() {
        UUID companyId = UUID.randomUUID();

        TenantContext.setCompanyId(companyId);

        assertEquals(
                companyId,
                TenantContext.requireCompanyId()
        );
    }

    @Test
    void shouldThrowWhenCompanyIdIsMissing() {
        assertThrows(
                IllegalStateException.class,
                TenantContext::requireCompanyId
        );
    }

    @Test
    void shouldClearCompanyContext() {
        UUID companyId = UUID.randomUUID();

        TenantContext.setCompanyId(companyId);

        TenantContext.clear();

        assertNull(TenantContext.getCompanyId());
    }
}