package com.kos.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import com.kos.dto.Order;
import com.kos.service.OrderService;

@RestController
public class OrderController {

//    @Autowired
//    OrderService orderService;
//
//    @PostMapping("/createOrder")
//    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
//
//        return new ResponseEntity<Order>(
//                orderService.createOrder(order),
//                HttpStatus.OK
//        );
//    }
//    @Autowired
//    OrderRepository orderRepository;
//
//    @GetMapping("/orders")
//    public ResponseEntity<List<Order>> getOrdersByStatus(
//            @RequestParam String status) {
//
//        return new ResponseEntity<>(
//                orderRepository.findByOrderStatus(status),
//                HttpStatus.OK
//        );
//    }
//    

}
