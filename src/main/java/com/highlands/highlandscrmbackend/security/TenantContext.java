package com.highlands.highlandscrmbackend.security;

import java.util.UUID;

public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_COMPANY = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setCompanyId(UUID companyId) {
        CURRENT_COMPANY.set(companyId);
    }

    public static UUID getCompanyId() {
        return CURRENT_COMPANY.get();
    }

    public static UUID requireCompanyId() {
        UUID companyId = CURRENT_COMPANY.get();

        if (companyId == null) {
            throw new IllegalStateException(
                    "No company context is available for the current request"
            );
        }

        return companyId;
    }

    public static void clear() {
        CURRENT_COMPANY.remove();
    }
}