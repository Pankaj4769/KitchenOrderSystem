package com.kos.service;

import com.kos.dto.TableSession;

import java.util.List;

public interface TableSessionService {

    /**
     * Returns the existing ACTIVE session for the table, or creates a new one.
     * This is the single entry point for all devices — guarantees they share one sessionId.
     */
    TableSession getOrCreate(Long tableId, String tableName, String waiterName, String restaurantId);

    /** Consume the current KOT round and return it, then increment for next call. */
    int nextKotRound(String sessionId);

    /** Mark session as BILL_REQUESTED. */
    TableSession requestBill(String sessionId);

    /** Close the session (CLOSED). */
    TableSession close(String sessionId);

    /** Fetch a session by its ID. */
    TableSession getById(String sessionId);

    /** All open sessions (ACTIVE + BILL_REQUESTED) for a restaurant. */
    List<TableSession> getActiveSessions(String restaurantId);
}
