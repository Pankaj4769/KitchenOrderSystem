package com.kos.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import com.kos.dto.DashboardResponse;
import com.kos.service.DashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger logger = LogManager.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/data/{id}")
    public ResponseEntity<DashboardResponse> getDashboard(
            @PathVariable Long id,
            @RequestParam(defaultValue = "today") String dateRange) {
        logger.info("Entering getDashboard() with id={}", id);
        try {
            ResponseEntity<DashboardResponse> result = ResponseEntity.ok(dashboardService.getDashboardData(id, dateRange));
            logger.info("Exiting getDashboard()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getDashboard(): {}", e.getMessage(), e);
            throw e;
        }
    }
}
