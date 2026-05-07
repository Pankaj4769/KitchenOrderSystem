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

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DashboardService {

    @Autowired private OrderRepository orderRepo;
    @Autowired private OrderItemRepository itemRepo;
    @Autowired private PaymentDataRepository paymentRepo;
    //@Autowired private TableRepository tableRepo;

    public DashboardResponse getDashboardData(Long restaurantId) {

        DashboardResponse res = new DashboardResponse();
        res.setLastUpdated(LocalDateTime.now());

        // METRICS
        Metrics m = new Metrics();
        long totalOrders = orderRepo.countTotalOrders(restaurantId);
        double revenue = 0;
        List<PaymentData> list = paymentRepo.findByRestaurantId(restaurantId.toString());
        for(PaymentData p: list) {
        	revenue = revenue + p.getAmount();
        }

        m.setTotalOrders(totalOrders);
        m.setTotalRevenue(revenue);
        m.setAvgOrderValue(totalOrders == 0 ? 0 : revenue / totalOrders);
        m.setCompletedOrders(orderRepo.completedOrders(restaurantId));
        m.setGrowthPercentage(12.5); // mock

        res.setMetrics(m);

        // STATUS
        OrdersByStatus status = new OrdersByStatus();
        for (Object[] row : orderRepo.countByStatus(restaurantId)) {
            String s = row[0].toString();
            long count = (long) row[1];

            switch (s) {
                case "PENDING" -> status.setPending(count);
                case "PREPARING" -> status.setPreparing(count);
                case "READY" -> status.setReady(count);
            }
        }
        res.setOrdersByStatus(status);

        // TOP SELLING
        List<TopSellingItem> items = new ArrayList<>();
        for (Object[] r : itemRepo.topSelling(restaurantId.toString())) {
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
