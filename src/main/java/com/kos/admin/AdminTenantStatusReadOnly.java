package com.kos.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Read-only mirror of the {@code admin_tenant_status} table owned by the
 * kos-admin-api service. Used here only to enforce tenant suspensions
 * in the customer login flow.
 *
 * <p>Kos does NOT write to this table — admin operations stay in the
 * admin BFF. We just need to know whether a tenant is suspended.</p>
 */
@Entity
@Table(name = "admin_tenant_status")
public class AdminTenantStatusReadOnly {

    /** Same value as {@code restaurent.restaurent_id}. */
    @Id
    private Integer tenantId;

    /** "ACTIVE" or "SUSPENDED" — kept as String to avoid enum coupling. */
    @Column(nullable = false, length = 32)
    private String status;

    public Integer getTenantId() { return tenantId; }
    public String getStatus() { return status; }
}
