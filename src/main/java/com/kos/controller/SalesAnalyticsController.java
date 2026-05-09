package com.kos.controller;

import com.kos.dto.SalesReportResponse;
import com.kos.service.SalesAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/reports/sales")
public class SalesAnalyticsController {

    @Autowired
    private SalesAnalyticsService salesAnalyticsService;

    @GetMapping
    public ResponseEntity<SalesReportResponse> getSalesReport(
            @RequestParam String restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate,
            @RequestParam(defaultValue = "STARTER") String plan) {
        
        java.time.LocalDateTime localStart = startDate.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        java.time.LocalDateTime localEnd = endDate.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();

        SalesReportResponse response = salesAnalyticsService.generateReport(restaurantId, localStart, localEnd, plan);
        return ResponseEntity.ok(response);
    }
}
