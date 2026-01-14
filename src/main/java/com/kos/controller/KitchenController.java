package com.kos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.kos.dto.Order;
import com.kos.service.KitchenServiceImpl;

@RestController
@RequestMapping("/kitchen")
public class KitchenController {

//    @Autowired
//    KitchenServiceImpl kitchenService;
//
//    @PatchMapping("/order/{orderId}/status")
//    public Order updateStatus(
//            @PathVariable Integer orderId,
//            @RequestParam String status) {
//
//        return kitchenService.updateOrderStatus(orderId, status);
//    }
}
