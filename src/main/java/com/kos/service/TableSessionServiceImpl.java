package com.kos.service;

import com.kos.dto.TableSession;
import com.kos.repository.TableSessionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class TableSessionServiceImpl implements TableSessionService {

    private static final Logger logger = LogManager.getLogger(TableSessionServiceImpl.class);

    private final TableSessionRepository repo;

    public TableSessionServiceImpl(TableSessionRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public TableSession getOrCreate(Long tableId, String tableName, String waiterName, String restaurantId) {
        logger.info("Entering getOrCreate() with tableId={}, restaurantId={}", tableId, restaurantId);
        try {
            TableSession result = repo.findByTableIdAndRestaurantIdAndStatus(tableId, restaurantId, "ACTIVE")
                    .map(existing -> {
                        // Update waiter name if a new one is provided
                        if (waiterName != null && !waiterName.isBlank()
                                && !waiterName.equals(existing.getWaiterName())) {
                            existing.setWaiterName(waiterName);
                            return repo.save(existing);
                        }
                        return existing;
                    })
                    .orElseGet(() -> {
                        TableSession s = new TableSession();
                        s.setSessionId("SES-" + tableId + "-" + System.currentTimeMillis());
                        s.setTableId(tableId);
                        s.setTableName(tableName);
                        s.setWaiterName(waiterName);
                        s.setStatus("ACTIVE");
                        s.setKotRound(1);
                        s.setRestaurantId(restaurantId);
                        s.setOpenedAt(LocalDateTime.now());
                        return repo.save(s);
                    });
            logger.info("Exiting getOrCreate()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getOrCreate(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public int nextKotRound(String sessionId) {
        logger.info("Entering nextKotRound()");
        try {
            TableSession s = load(sessionId);
            int round = s.getKotRound();
            s.setKotRound(round + 1);
            repo.save(s);
            logger.info("Exiting nextKotRound()");
            return round;
        } catch (RuntimeException e) {
            logger.error("Error in nextKotRound(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public TableSession requestBill(String sessionId) {
        logger.info("Entering requestBill()");
        try {
            TableSession s = load(sessionId);
            s.setStatus("BILL_REQUESTED");
            TableSession result = repo.save(s);
            logger.info("Exiting requestBill()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in requestBill(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public TableSession close(String sessionId) {
        logger.info("Entering close()");
        try {
            TableSession s = load(sessionId);
            s.setStatus("CLOSED");
            s.setClosedAt(LocalDateTime.now());
            TableSession result = repo.save(s);
            logger.info("Exiting close()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in close(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public TableSession getById(String sessionId) {
        logger.info("Entering getById()");
        try {
            TableSession result = load(sessionId);
            logger.info("Exiting getById()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getById(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<TableSession> getActiveSessions(String restaurantId) {
        logger.info("Entering getActiveSessions() with restaurantId={}", restaurantId);
        try {
            List<TableSession> result = repo.findByRestaurantIdAndStatusIn(
                    restaurantId, Arrays.asList("ACTIVE", "BILL_REQUESTED"));
            logger.info("Exiting getActiveSessions()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getActiveSessions(): {}", e.getMessage(), e);
            throw e;
        }
    }

    private TableSession load(String sessionId) {
        return repo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Session not found: " + sessionId));
    }
}
