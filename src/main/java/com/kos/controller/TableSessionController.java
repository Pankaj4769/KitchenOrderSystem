package com.kos.controller;

import com.kos.dto.TableSession;
import com.kos.service.TableSessionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sessions")
public class TableSessionController {

    private static final Logger logger = LogManager.getLogger(TableSessionController.class);

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
        logger.info("Entering getOrCreate()");
        try {
            Long tableId       = Long.valueOf(body.get("tableId").toString());
            String tableName   = (String) body.get("tableName");
            String waiterName  = (String) body.getOrDefault("waiterName", null);
            String restaurantId = (String) body.get("restaurantId");
            ResponseEntity<TableSession> result = ResponseEntity.ok(sessionService.getOrCreate(tableId, tableName, waiterName, restaurantId));
            logger.info("Exiting getOrCreate()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getOrCreate(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * GET /sessions/active?restaurantId=X
     * Returns all ACTIVE + BILL_REQUESTED sessions for a restaurant.
     * Used by the owner dashboard to sync table states.
     */
    @GetMapping("/active")
    public ResponseEntity<List<TableSession>> getActiveSessions(@RequestParam String restaurantId) {
        logger.info("Entering getActiveSessions() with restaurantId={}", restaurantId);
        try {
            ResponseEntity<List<TableSession>> result = ResponseEntity.ok(sessionService.getActiveSessions(restaurantId));
            logger.info("Exiting getActiveSessions()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getActiveSessions(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * GET /sessions/{sessionId}
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<TableSession> getById(@PathVariable String sessionId) {
        logger.info("Entering getById()");
        try {
            ResponseEntity<TableSession> result = ResponseEntity.ok(sessionService.getById(sessionId));
            logger.info("Exiting getById()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getById(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * POST /sessions/{sessionId}/next-kot
     * Increments kotRound and returns the round number that was consumed.
     * Body: {} (empty)
     */
    @PostMapping("/{sessionId}/next-kot")
    public ResponseEntity<Map<String, Integer>> nextKot(@PathVariable String sessionId) {
        logger.info("Entering nextKot()");
        try {
            int round = sessionService.nextKotRound(sessionId);
            ResponseEntity<Map<String, Integer>> result = ResponseEntity.ok(Map.of("kotRound", round));
            logger.info("Exiting nextKot()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in nextKot(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * PATCH /sessions/{sessionId}/bill-request
     * Marks session as BILL_REQUESTED — table grid turns red on all devices via SSE.
     */
    @PatchMapping("/{sessionId}/bill-request")
    public ResponseEntity<TableSession> requestBill(@PathVariable String sessionId) {
        logger.info("Entering requestBill()");
        try {
            ResponseEntity<TableSession> result = ResponseEntity.ok(sessionService.requestBill(sessionId));
            logger.info("Exiting requestBill()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in requestBill(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * PATCH /sessions/{sessionId}/close
     * Closes the session — table becomes AVAILABLE on all devices.
     */
    @PatchMapping("/{sessionId}/close")
    public ResponseEntity<TableSession> close(@PathVariable String sessionId) {
        logger.info("Entering close()");
        try {
            ResponseEntity<TableSession> result = ResponseEntity.ok(sessionService.close(sessionId));
            logger.info("Exiting close()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in close(): {}", e.getMessage(), e);
            throw e;
        }
    }
}
