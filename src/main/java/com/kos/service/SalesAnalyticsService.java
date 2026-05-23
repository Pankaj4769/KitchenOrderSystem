package com.kos.service;

import com.kos.dto.SalesReportResponse;
import com.kos.repository.OrderItemRepository;
import com.kos.repository.OrderRepository;
import com.kos.repository.PaymentDataRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesAnalyticsService {

    private static final Logger logger = LogManager.getLogger(SalesAnalyticsService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentDataRepository paymentDataRepository;

    @Autowired
    private com.kos.repository.RestaurentRepository restaurentRepository;

    @Autowired
    private DashboardService dashboardService;

    public SalesReportResponse generateReport(String restaurantId, LocalDateTime start, LocalDateTime end, String plan) {
        logger.info("Entering generateReport() with restaurantId={}", restaurantId);
        try {
            SalesReportResponse response = new SalesReportResponse();

            // Derive dateRange string from the start/end window so DashboardService applies the same filter
            long daySpan = java.time.temporal.ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
            String dateRange = daySpan <= 1 ? "today" : daySpan <= 7 ? "week" : "month";

            // Use DashboardService to get accurate base data
            com.kos.dto.DashboardResponse dashData = dashboardService.getDashboardData(Long.parseLong(restaurantId), dateRange);

            // Fetch Restaurant Details
            com.kos.dto.Restaurent rest = restaurentRepository.findById(Integer.parseInt(restaurantId)).orElse(null);
            if (rest != null) {
                response.setRestaurantName(rest.getRestaurentName());
                // Restaurent entity doesn't have address yet, so default to Main Branch
                response.setRestaurantAddress("Main Branch");
            }

            // KPIs
            SalesReportResponse.SalesKpi kpi = new SalesReportResponse.SalesKpi();
            long totalOrders = dashData.getMetrics().getTotalOrders();
            long completedOrders = dashData.getMetrics().getCompletedOrders();
            long cancelledOrders = orderRepository.countCancelledOrdersByDate(restaurantId, start, end); // Keep date filtered for cancelled

            Double grossSales = dashData.getMetrics().getTotalRevenue();
            Double netSales = grossSales;

            kpi.setGrossSales(grossSales);
            kpi.setNetSales(netSales);
            kpi.setTotalOrders(totalOrders);
            kpi.setCompletedOrders(completedOrders);
            kpi.setCancelledOrders(cancelledOrders);
            kpi.setAvgOrderValue(completedOrders == 0 ? 0 : netSales / completedOrders);
            kpi.setTaxCollected(0.0); // Extend schema if needed
            response.setMetrics(kpi);

            // Payment Breakdown
            java.time.Instant startInstant = start.atZone(ZoneId.systemDefault()).toInstant();
            java.time.Instant endInstant = end.atZone(ZoneId.systemDefault()).toInstant();
            List<Object[]> pbRows = paymentDataRepository.getPaymentBreakdownByDate(restaurantId, startInstant, endInstant);
            List<SalesReportResponse.PaymentBreakdown> pbList = new ArrayList<>();
            for (Object[] row : pbRows) {
                SalesReportResponse.PaymentBreakdown pb = new SalesReportResponse.PaymentBreakdown();
                pb.setMethod(row[0] == null ? "Unknown" : row[0].toString());
                pb.setOrderCount((Long) row[1]);
                pb.setAmount((Double) row[2]);
                pb.setPercentage(netSales == 0 ? 0 : (pb.getAmount() / netSales) * 100);
                pbList.add(pb);
            }
            response.setPaymentBreakdown(pbList);

            // Order Type Breakdown
            List<Object[]> otRows = orderRepository.getOrderTypeBreakdownByDate(restaurantId, start, end);
            List<SalesReportResponse.OrderTypeBreakdown> otList = new ArrayList<>();
            for (Object[] row : otRows) {
                SalesReportResponse.OrderTypeBreakdown ot = new SalesReportResponse.OrderTypeBreakdown();
                ot.setType(row[0] == null ? "Unknown" : row[0].toString());
                ot.setCount((Long) row[1]);
                ot.setRevenue((Double) row[2]);
                ot.setPercentage(grossSales == 0 ? 0 : (ot.getRevenue() / grossSales) * 100);
                otList.add(ot);
            }
            response.setOrderTypeBreakdown(otList);

            // Staff Sales
            List<Object[]> staffRows = orderRepository.getWaiterStatsByDate(restaurantId, start, end);
            List<SalesReportResponse.StaffSales> staffList = new ArrayList<>();
            for (Object[] row : staffRows) {
                SalesReportResponse.StaffSales ss = new SalesReportResponse.StaffSales();
                ss.setName(row[0] == null ? "Unknown" : row[0].toString());
                ss.setOrdersHandled((Long) row[1]);
                ss.setRevenue((Double) row[2]);
                staffList.add(ss);
            }
            response.setStaffSales(staffList);

            // Top Selling Items (from DashboardService to match exactly)
            List<SalesReportResponse.TopSellingItem> itemList = new ArrayList<>();
            if (dashData.getTopSellingItems() != null) {
                for (com.kos.dto.TopSellingItem item : dashData.getTopSellingItems()) {
                    SalesReportResponse.TopSellingItem tsi = new SalesReportResponse.TopSellingItem();
                    tsi.setName(item.getName());
                    tsi.setCategory(item.getCategory());
                    tsi.setQuantitySold(item.getQuantity());
                    tsi.setGrossAmount(item.getRevenue());
                    itemList.add(tsi);
                }
            }
            response.setTopSellingItems(itemList);

            // Revenue by Hour
            List<Object[]> hourRows = orderRepository.getRevenueByHourByDate(restaurantId, start, end);
            List<SalesReportResponse.HourlyRevenue> hourList = new ArrayList<>();
            for (Object[] row : hourRows) {
                SalesReportResponse.HourlyRevenue hr = new SalesReportResponse.HourlyRevenue();
                hr.setHour((Integer) row[0]);
                hr.setAmount((Double) row[1]);
                hourList.add(hr);
            }
            response.setRevenueByHour(hourList);

            logger.info("Exiting generateReport()");
            return response;
        } catch (RuntimeException e) {
            logger.error("Error in generateReport(): {}", e.getMessage(), e);
            throw e;
        }
    }
}
