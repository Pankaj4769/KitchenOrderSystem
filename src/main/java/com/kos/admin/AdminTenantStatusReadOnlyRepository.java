package com.kos.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Read-only access to admin_tenant_status. The admin BFF owns this table;
 * kos only queries it during login to enforce suspensions.
 */
public interface AdminTenantStatusReadOnlyRepository
        extends JpaRepository<AdminTenantStatusReadOnly, Integer> {

    Optional<AdminTenantStatusReadOnly> findByTenantId(Integer tenantId);
}
