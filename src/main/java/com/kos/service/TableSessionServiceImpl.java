package com.kos.service;

import com.kos.dto.TableSession;
import com.kos.repository.TableSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class TableSessionServiceImpl implements TableSessionService {

    private final TableSessionRepository repo;

    public TableSessionServiceImpl(TableSessionRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public TableSession getOrCreate(Long tableId, String tableName, String waiterName, String restaurantId) {
        return repo.findByTableIdAndRestaurantIdAndStatus(tableId, restaurantId, "ACTIVE")
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
    }

    @Override
    @Transactional
    public int nextKotRound(String sessionId) {
        TableSession s = load(sessionId);
        int round = s.getKotRound();
        s.setKotRound(round + 1);
        repo.save(s);
        return round;
    }

    @Override
    @Transactional
    public TableSession requestBill(String sessionId) {
        TableSession s = load(sessionId);
        s.setStatus("BILL_REQUESTED");
        return repo.save(s);
    }

    @Override
    @Transactional
    public TableSession close(String sessionId) {
        TableSession s = load(sessionId);
        s.setStatus("CLOSED");
        s.setClosedAt(LocalDateTime.now());
        return repo.save(s);
    }

    @Override
    public TableSession getById(String sessionId) {
        return load(sessionId);
    }

    @Override
    public List<TableSession> getActiveSessions(String restaurantId) {
        return repo.findByRestaurantIdAndStatusIn(
                restaurantId, Arrays.asList("ACTIVE", "BILL_REQUESTED"));
    }

    private TableSession load(String sessionId) {
        return repo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Session not found: " + sessionId));
    }
}
