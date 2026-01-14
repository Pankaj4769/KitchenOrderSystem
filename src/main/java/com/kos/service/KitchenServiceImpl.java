package com.kos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kos.controller.OrderSseController;
import com.kos.dto.Order;


@Service
public class KitchenServiceImpl {

//    @Autowired
//    OrderRepository orderRepository;
//
//    @Autowired
//    OrderSseController sseController;
//
//    public Order updateOrderStatus(Integer orderId, String status) {
//
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        order.setOrderStatus(status);
//
//        Order updatedOrder = orderRepository.save(order);
//
//        // 🔥 SEND LIVE UPDATE
//        sseController.sendOrderUpdate(updatedOrder);
//
//        return updatedOrder;
//    }
}
