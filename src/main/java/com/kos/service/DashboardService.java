package com.kos.service;

import org.springframework.stereotype.Service;

import com.kos.dto.DashboardResponse;
import com.kos.dto.Metrics;
import com.kos.dto.OrdersByStatus;
import com.kos.dto.PaymentData;
import com.kos.dto.TopSellingItem;
import com.kos.repository.OrderItemRepository;
import com.kos.repository.OrderRepository;
import com.kos.repository.PaymentDataRepository;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class DashboardService {

    @Autowired private OrderRepository orderRepo;
    @Autowired private OrderItemRepository itemRepo;
    @Autowired private PaymentDataRepository paymentRepo;
    //@Autowired private TableRepository tableRepo;

    public DashboardResponse getDashboardData(Long restaurantId, String dateRange) {

        DashboardResponse res = new DashboardResponse();
        res.setLastUpdated(LocalDateTime.now());

        // Compute date window based on filter
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = switch (dateRange) {
            case "week"  -> LocalDate.now().minusDays(7).atStartOfDay();
            case "month" -> LocalDate.now().minusMonths(1).atStartOfDay();
            default      -> LocalDate.now().atStartOfDay(); // "today"
        };
        String ridStr = restaurantId.toString();

        // METRICS (date-filtered)
        // Revenue comes from PaymentData (actual collected payments) not order status,
        // because orders may be paid without being explicitly marked SERVED.
        java.time.Instant startInstant = start.atZone(ZoneId.systemDefault()).toInstant();
        java.time.Instant endInstant   = end.atZone(ZoneId.systemDefault()).toInstant();

        Metrics m = new Metrics();
        long totalOrders = orderRepo.countTotalOrdersByDate(ridStr, start, end);
        Double revenue = paymentRepo.sumAmountByDateRange(ridStr, startInstant, endInstant);
        if (revenue == null) revenue = 0.0;

        m.setTotalOrders(totalOrders);
        m.setTotalRevenue(revenue);
        m.setAvgOrderValue(totalOrders == 0 ? 0 : revenue / totalOrders);
        m.setCompletedOrders(orderRepo.countPaidOrdersByDate(ridStr, start, end));
        m.setGrowthPercentage(12.5); // mock

        res.setMetrics(m);

        // STATUS (live pipeline — intentionally not date-filtered)
        OrdersByStatus status = new OrdersByStatus();
        for (Object[] row : orderRepo.countByStatus(restaurantId)) {
            String s = row[0].toString();
            long count = (long) row[1];

            switch (s) {
                case "PENDING"   -> status.setPending(count);
                case "PREPARING" -> status.setPreparing(count);
                case "READY"     -> status.setReady(count);
            }
        }
        res.setOrdersByStatus(status);

        // TOP SELLING — uses paymentStatus=PAID to match revenue calculation
        List<TopSellingItem> items = new ArrayList<>();
        for (Object[] r : itemRepo.getTopSellingByPaidOrdersByDate(ridStr, start, end)) {
            TopSellingItem i = new TopSellingItem();
            i.setName((String) r[0]);
            i.setCategory((String) r[1]);
            i.setQuantity((Long) r[2]);
            i.setRevenue((Double) r[3]);
            items.add(i);
        }
        res.setTopSellingItems(items);

        return res;
    }
}
