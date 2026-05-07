package com.kos.controller;

import java.util.List;
import java.util.Map;

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

    @Autowired
    TableManagementService tableManagementService;

    // GET /api/tables/{restaurantId}
    @GetMapping("/{restaurantId}")
    public ResponseEntity<List<RestaurantTable>> getAllTables(@PathVariable String restaurantId) {
        return new ResponseEntity<>(tableManagementService.getAllTables(restaurantId), HttpStatus.OK);
    }

    // GET /api/tables/table/{id}
    @GetMapping("/table/{id}")
    public ResponseEntity<RestaurantTable> getTableById(@PathVariable Long id) {
        return new ResponseEntity<>(tableManagementService.getTableById(id), HttpStatus.OK);
    }

    // POST /api/tables
    @PostMapping
    public ResponseEntity<RestaurantTable> addTable(@RequestBody RestaurantTable table) {
        return new ResponseEntity<>(tableManagementService.addTable(table), HttpStatus.CREATED);
    }

    // DELETE /api/tables/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTable(@PathVariable Long id) {
        return new ResponseEntity<>(tableManagementService.deleteTable(id), HttpStatus.OK);
    }

    // PUT /api/tables/{id}/occupy
    @PutMapping("/{id}/occupy")
    public ResponseEntity<RestaurantTable> occupyTable(
            @PathVariable Long id,
            @RequestBody OccupyTableRequest request) {
        return new ResponseEntity<>(tableManagementService.occupyTable(id, request), HttpStatus.OK);
    }

    // PUT /api/tables/{id}/release
    @PutMapping("/{id}/release")
    public ResponseEntity<RestaurantTable> releaseTable(@PathVariable Long id) {
        return new ResponseEntity<>(tableManagementService.releaseTable(id), HttpStatus.OK);
    }

    // PUT /api/tables/{id}/status
    @PutMapping("/{id}/status")
    public ResponseEntity<RestaurantTable> updateTableStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return new ResponseEntity<>(
                tableManagementService.updateTableStatus(id, body.get("status")), HttpStatus.OK);
    }

    // PUT /api/tables/{id}/area
    @PutMapping("/{id}/area")
    public ResponseEntity<RestaurantTable> updateTableArea(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return new ResponseEntity<>(
                tableManagementService.updateTableArea(id, body.get("areaName")), HttpStatus.OK);
    }

    // PUT /api/tables/bulk-status
    @PutMapping("/bulk-status")
    public ResponseEntity<MessageResponse> bulkUpdateStatus(@RequestBody BulkStatusRequest request) {
        return new ResponseEntity<>(tableManagementService.bulkUpdateStatus(request), HttpStatus.OK);
    }
}
