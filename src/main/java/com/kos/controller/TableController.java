package com.kos.controller;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kos.dto.BulkStatusRequest;
import com.kos.dto.MessageResponse;
import com.kos.dto.OccupyTableRequest;
import com.kos.dto.RestaurantTable;
import com.kos.service.TableManagementService;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    private static final Logger logger = LogManager.getLogger(TableController.class);

    @Autowired
    TableManagementService tableManagementService;

    // GET /api/tables/{restaurantId}
    @GetMapping("/{restaurantId}")
    public ResponseEntity<List<RestaurantTable>> getAllTables(@PathVariable String restaurantId) {
        logger.info("Entering getAllTables() with restaurantId={}", restaurantId);
        try {
            ResponseEntity<List<RestaurantTable>> result = new ResponseEntity<>(tableManagementService.getAllTables(restaurantId), HttpStatus.OK);
            logger.info("Exiting getAllTables()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getAllTables(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // GET /api/tables/table/{id}
    @GetMapping("/table/{id}")
    public ResponseEntity<RestaurantTable> getTableById(@PathVariable Long id) {
        logger.info("Entering getTableById() with id={}", id);
        try {
            ResponseEntity<RestaurantTable> result = new ResponseEntity<>(tableManagementService.getTableById(id), HttpStatus.OK);
            logger.info("Exiting getTableById()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getTableById(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // POST /api/tables
    @PostMapping
    public ResponseEntity<RestaurantTable> addTable(@RequestBody RestaurantTable table) {
        logger.info("Entering addTable()");
        try {
            ResponseEntity<RestaurantTable> result = new ResponseEntity<>(tableManagementService.addTable(table), HttpStatus.CREATED);
            logger.info("Exiting addTable()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in addTable(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // DELETE /api/tables/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTable(@PathVariable Long id) {
        logger.info("Entering deleteTable() with id={}", id);
        try {
            ResponseEntity<MessageResponse> result = new ResponseEntity<>(tableManagementService.deleteTable(id), HttpStatus.OK);
            logger.info("Exiting deleteTable()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in deleteTable(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // PUT /api/tables/{id}/occupy
    @PutMapping("/{id}/occupy")
    public ResponseEntity<RestaurantTable> occupyTable(
            @PathVariable Long id,
            @RequestBody OccupyTableRequest request) {
        logger.info("Entering occupyTable() with id={}", id);
        try {
            ResponseEntity<RestaurantTable> result = new ResponseEntity<>(tableManagementService.occupyTable(id, request), HttpStatus.OK);
            logger.info("Exiting occupyTable()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in occupyTable(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // PUT /api/tables/{id}/release
    @PutMapping("/{id}/release")
    public ResponseEntity<RestaurantTable> releaseTable(@PathVariable Long id) {
        logger.info("Entering releaseTable() with id={}", id);
        try {
            ResponseEntity<RestaurantTable> result = new ResponseEntity<>(tableManagementService.releaseTable(id), HttpStatus.OK);
            logger.info("Exiting releaseTable()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in releaseTable(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // PUT /api/tables/{id}/status
    @PutMapping("/{id}/status")
    public ResponseEntity<RestaurantTable> updateTableStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        logger.info("Entering updateTableStatus() with id={}", id);
        try {
            ResponseEntity<RestaurantTable> result = new ResponseEntity<>(
                    tableManagementService.updateTableStatus(id, body.get("status")), HttpStatus.OK);
            logger.info("Exiting updateTableStatus()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in updateTableStatus(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // PUT /api/tables/{id}/area
    @PutMapping("/{id}/area")
    public ResponseEntity<RestaurantTable> updateTableArea(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        logger.info("Entering updateTableArea() with id={}", id);
        try {
            ResponseEntity<RestaurantTable> result = new ResponseEntity<>(
                    tableManagementService.updateTableArea(id, body.get("areaName")), HttpStatus.OK);
            logger.info("Exiting updateTableArea()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in updateTableArea(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // PUT /api/tables/bulk-status
    @PutMapping("/bulk-status")
    public ResponseEntity<MessageResponse> bulkUpdateStatus(@RequestBody BulkStatusRequest request) {
        logger.info("Entering bulkUpdateStatus()");
        try {
            ResponseEntity<MessageResponse> result = new ResponseEntity<>(tableManagementService.bulkUpdateStatus(request), HttpStatus.OK);
            logger.info("Exiting bulkUpdateStatus()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in bulkUpdateStatus(): {}", e.getMessage(), e);
            throw e;
        }
    }
}
