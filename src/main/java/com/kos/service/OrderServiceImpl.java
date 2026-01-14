package com.kos.service;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kos.controller.OrderSseController;
import com.kos.dto.Item;
import com.kos.dto.Order;
import com.kos.dto.OrderItem;


@Service
public class OrderServiceImpl implements OrderService {

	@Override
	public Order createOrder(Order order) {
		// TODO Auto-generated method stub
		return null;
	}
	
//	@Autowired
//    OrderRepository orderRepository;
//
//    @Autowired
//    InventoryService inventoryService;
//    
//    @Autowired
//    private OrderSseController sseController;
//
//    @Override
//    public Order createOrder(Order order) {
//    	
//    	OrderValidator.validate(order);
//
//        // 1. Set initial order details
//        order.setOrderStatus("NEW");
//        order.setOrderTime(LocalDateTime.now());
//
//        // 2. Deduct inventory for each order item
//        if (order.getOrderItems() != null) {
//            for (OrderItem orderItem : order.getOrderItems()) {
//
//                Item item = new Item();
//                item.setItemId(orderItem.getItemId());
//
//                // Deduct stock (negative quantity)
//                item.setItemQuantity(-orderItem.getQuantity());
//
//                inventoryService.restockItem(item);
//            }
//        }
//
//        // 3. Save order
//        Order savedOrder = orderRepository.save(order);
//        sseController.sendOrderUpdate(savedOrder);
//        return orderRepository.save(order);
//    }
}
