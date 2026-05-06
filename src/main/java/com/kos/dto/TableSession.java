package com.kos.dto;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kos_table_sessions")
public class TableSession {

    /** Format: SES-{tableId}-{epochMillis} — generated server-side */
    @Id
    private String sessionId;

    @Column(nullable = false)
    private Long tableId;

    private String tableName;

    private String waiterName;

    /** ACTIVE | BILL_REQUESTED | CLOSED */
    @Column(nullable = false)
    private String status = "ACTIVE";

    /** Next KOT round counter — incremented on every KOT for this session */
    @Column(nullable = false)
    private Integer kotRound = 1;

    @Column(nullable = false)
    private String restaurantId;

    private LocalDateTime openedAt;

    private LocalDateTime closedAt;

    // ── Getters & Setters ──────────────────────────────────

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getWaiterName() { return waiterName; }
    public void setWaiterName(String waiterName) { this.waiterName = waiterName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getKotRound() { return kotRound; }
    public void setKotRound(Integer kotRound) { this.kotRound = kotRound; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public LocalDateTime getOpenedAt() { return openedAt; }
    public void setOpenedAt(LocalDateTime openedAt) { this.openedAt = openedAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
}
