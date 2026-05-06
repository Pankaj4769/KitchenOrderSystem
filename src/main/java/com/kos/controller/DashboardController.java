package com.kos.controller;

import org.springframework.web.bind.annotation.*;

import com.kos.dto.DashboardResponse;
import com.kos.service.DashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/data/{id}")
    public ResponseEntity<DashboardResponse> getDashboard(
            @PathVariable Long id) {

        return ResponseEntity.ok(dashboardService.getDashboardData(id));
    }
}
