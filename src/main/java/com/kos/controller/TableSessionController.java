package com.kos.controller;

import com.kos.dto.TableSession;
import com.kos.service.TableSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sessions")
public class TableSessionController {

    private final TableSessionService sessionService;

    public TableSessionController(TableSessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * POST /sessions/get-or-create
     * Body: { tableId, tableName, waiterName, restaurantId }
     * Returns the existing ACTIVE session or creates a new one.
     * All devices calling this for the same table get the SAME sessionId.
     */
    @PostMapping("/get-or-create")
    public ResponseEntity<TableSession> getOrCreate(@RequestBody Map<String, Object> body) {
        Long tableId       = Long.valueOf(body.get("tableId").toString());
        String tableName   = (String) body.get("tableName");
        String waiterName  = (String) body.getOrDefault("waiterName", null);
        String restaurantId = (String) body.get("restaurantId");
        return ResponseEntity.ok(sessionService.getOrCreate(tableId, tableName, waiterName, restaurantId));
    }

    /**
     * GET /sessions/active?restaurantId=X
     * Returns all ACTIVE + BILL_REQUESTED sessions for a restaurant.
     * Used by the owner dashboard to sync table states.
     */
    @GetMapping("/active")
    public ResponseEntity<List<TableSession>> getActiveSessions(@RequestParam String restaurantId) {
        return ResponseEntity.ok(sessionService.getActiveSessions(restaurantId));
    }

    /**
     * GET /sessions/{sessionId}
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<TableSession> getById(@PathVariable String sessionId) {
        return ResponseEntity.ok(sessionService.getById(sessionId));
    }

    /**
     * POST /sessions/{sessionId}/next-kot
     * Increments kotRound and returns the round number that was consumed.
     * Body: {} (empty)
     */
    @PostMapping("/{sessionId}/next-kot")
    public ResponseEntity<Map<String, Integer>> nextKot(@PathVariable String sessionId) {
        int round = sessionService.nextKotRound(sessionId);
        return ResponseEntity.ok(Map.of("kotRound", round));
    }

    /**
     * PATCH /sessions/{sessionId}/bill-request
     * Marks session as BILL_REQUESTED — table grid turns red on all devices via SSE.
     */
    @PatchMapping("/{sessionId}/bill-request")
    public ResponseEntity<TableSession> requestBill(@PathVariable String sessionId) {
        return ResponseEntity.ok(sessionService.requestBill(sessionId));
    }

    /**
     * PATCH /sessions/{sessionId}/close
     * Closes the session — table becomes AVAILABLE on all devices.
     */
    @PatchMapping("/{sessionId}/close")
    public ResponseEntity<TableSession> close(@PathVariable String sessionId) {
        return ResponseEntity.ok(sessionService.close(sessionId));
    }
}
