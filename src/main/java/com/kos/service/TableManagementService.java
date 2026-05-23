package com.kos.service;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(TableManagementService.class);

    @Autowired
    RestaurantTableRepository tableRepository;

    // ── GET ──────────────────────────────────────────────────────────
    public List<RestaurantTable> getAllTables(String restaurantId) {
        logger.info("Entering getAllTables() with restaurantId={}", restaurantId);
        try {
            List<RestaurantTable> result = tableRepository.findByRestaurantId(restaurantId);
            logger.info("Exiting getAllTables()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getAllTables(): {}", e.getMessage(), e);
            throw e;
        }
    }

    public RestaurantTable getTableById(Long id) {
        logger.info("Entering getTableById() with id={}", id);
        try {
            RestaurantTable result = tableRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Table not found with id: " + id));
            logger.info("Exiting getTableById()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getTableById(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // ── CREATE ───────────────────────────────────────────────────────
    public RestaurantTable addTable(RestaurantTable table) {
        logger.info("Entering addTable()");
        try {
            if (table.getStatus() == null) {
                table.setStatus(TableStatus.AVAILABLE);
            }
            RestaurantTable result = tableRepository.save(table);
            logger.info("Exiting addTable()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in addTable(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────
    public MessageResponse deleteTable(Long id) {
        logger.info("Entering deleteTable() with id={}", id);
        try {
            RestaurantTable table = getTableById(id);
            tableRepository.delete(table);
            MessageResponse result = new MessageResponse("Table deleted successfully", true);
            logger.info("Exiting deleteTable()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in deleteTable(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // ── OCCUPY ───────────────────────────────────────────────────────
    public RestaurantTable occupyTable(Long id, OccupyTableRequest request) {
        logger.info("Entering occupyTable() with id={}", id);
        try {
            RestaurantTable table = getTableById(id);
            table.setStatus(TableStatus.OCCUPIED);
            table.setOrderNumber(request.getOrderNumber());
            table.setWaiter(request.getWaiter());
            table.setStartTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now());
            RestaurantTable result = tableRepository.save(table);
            logger.info("Exiting occupyTable()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in occupyTable(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // ── RELEASE ──────────────────────────────────────────────────────
    public RestaurantTable releaseTable(Long id) {
        logger.info("Entering releaseTable() with id={}", id);
        try {
            RestaurantTable table = getTableById(id);
            table.setStatus(TableStatus.AVAILABLE);
            table.setOrderNumber(null);
            table.setWaiter(null);
            table.setStartTime(null);
            RestaurantTable result = tableRepository.save(table);
            logger.info("Exiting releaseTable()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in releaseTable(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // ── UPDATE STATUS ─────────────────────────────────────────────────
    public RestaurantTable updateTableStatus(Long id, String status) {
        logger.info("Entering updateTableStatus() with id={}", id);
        try {
            RestaurantTable table = getTableById(id);
            try {
                table.setStatus(TableStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid status value: " + status);
            }
            RestaurantTable result = tableRepository.save(table);
            logger.info("Exiting updateTableStatus()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in updateTableStatus(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // ── UPDATE AREA ───────────────────────────────────────────────────
    public RestaurantTable updateTableArea(Long id, String areaName) {
        logger.info("Entering updateTableArea() with id={}", id);
        try {
            RestaurantTable table = getTableById(id);
            table.setAreaName(areaName);
            RestaurantTable result = tableRepository.save(table);
            logger.info("Exiting updateTableArea()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in updateTableArea(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // ── BULK STATUS UPDATE ────────────────────────────────────────────
    public MessageResponse bulkUpdateStatus(BulkStatusRequest request) {
        logger.info("Entering bulkUpdateStatus()");
        try {
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
            MessageResponse result = new MessageResponse("Updated " + tables.size() + " tables successfully", true);
            logger.info("Exiting bulkUpdateStatus()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in bulkUpdateStatus(): {}", e.getMessage(), e);
            throw e;
        }
    }
}
