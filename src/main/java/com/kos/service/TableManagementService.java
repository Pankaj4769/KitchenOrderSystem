package com.kos.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.kos.dto.BulkStatusRequest;
import com.kos.dto.MessageResponse;
import com.kos.dto.OccupyTableRequest;
import com.kos.dto.RestaurantTable;
import com.kos.dto.RestaurantTable.TableStatus;
import com.kos.repository.RestaurantTableRepository;

@Service
public class TableManagementService {

    Logger logger = LoggerFactory.getLogger(TableManagementService.class);

    @Autowired
    RestaurantTableRepository tableRepository;

    // ── GET ──────────────────────────────────────────────────────────
    public List<RestaurantTable> getAllTables(String restaurantId) {
        return tableRepository.findByRestaurantId(restaurantId);
    }

    public RestaurantTable getTableById(Long id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Table not found with id: " + id));
    }

    // ── CREATE ───────────────────────────────────────────────────────
    public RestaurantTable addTable(RestaurantTable table) {
        if (table.getStatus() == null) {
            table.setStatus(TableStatus.AVAILABLE);
        }
        return tableRepository.save(table);
    }

    // ── DELETE ───────────────────────────────────────────────────────
    public MessageResponse deleteTable(Long id) {
        RestaurantTable table = getTableById(id);
        tableRepository.delete(table);
        return new MessageResponse("Table deleted successfully", true);
    }

    // ── OCCUPY ───────────────────────────────────────────────────────
    public RestaurantTable occupyTable(Long id, OccupyTableRequest request) {
        RestaurantTable table = getTableById(id);
        table.setStatus(TableStatus.OCCUPIED);
        table.setOrderNumber(request.getOrderNumber());
        table.setWaiter(request.getWaiter());
        table.setStartTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now());
        return tableRepository.save(table);
    }

    // ── RELEASE ──────────────────────────────────────────────────────
    public RestaurantTable releaseTable(Long id) {
        RestaurantTable table = getTableById(id);
        table.setStatus(TableStatus.AVAILABLE);
        table.setOrderNumber(null);
        table.setWaiter(null);
        table.setStartTime(null);
        return tableRepository.save(table);
    }

    // ── UPDATE STATUS ─────────────────────────────────────────────────
    public RestaurantTable updateTableStatus(Long id, String status) {
        RestaurantTable table = getTableById(id);
        try {
            table.setStatus(TableStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status value: " + status);
        }
        return tableRepository.save(table);
    }

    // ── UPDATE AREA ───────────────────────────────────────────────────
    public RestaurantTable updateTableArea(Long id, String areaName) {
        RestaurantTable table = getTableById(id);
        table.setAreaName(areaName);
        return tableRepository.save(table);
    }

    // ── BULK STATUS UPDATE ────────────────────────────────────────────
    public MessageResponse bulkUpdateStatus(BulkStatusRequest request) {
        TableStatus newStatus;
        try {
            newStatus = TableStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status value: " + request.getStatus());
        }
        List<RestaurantTable> tables = tableRepository.findAllById(request.getTableIds());
        for (RestaurantTable table : tables) {
            table.setStatus(newStatus);
        }
        tableRepository.saveAll(tables);
        return new MessageResponse("Updated " + tables.size() + " tables successfully", true);
    }
}
