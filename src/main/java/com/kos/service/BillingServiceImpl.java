package com.kos.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.kos.dto.Bill;
import com.kos.dto.BillItem;
import com.kos.dto.Order;
import com.kos.dto.OrderItem;


@Service
public class BillingServiceImpl implements BillingService {

	@Override
	public Bill generateBill(Integer orderId) {
		// TODO Auto-generated method stub
		return null;
	}

//    @Autowired
//    private BillRepository billRepository;
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @Override
//    public Bill generateBill(Integer orderId) {
//
//        // --------------------------------------------------------------------
//        // 1. Fetch Order (Bill is always generated from an Order)
//        // --------------------------------------------------------------------
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.NOT_FOUND,
//                        "Order with ID " + orderId + " not found"
//                ));
//
//        // --------------------------------------------------------------------
//        // 2. Prepare Bill Items & Calculate Subtotal
//        // --------------------------------------------------------------------
//        int subtotal = 0;
//        List<BillItem> billItems = new ArrayList<>();
//
//        if (order.getOrderItems() != null) {
//            for (OrderItem orderItem : order.getOrderItems()) {
//
//                // Create BillItem from OrderItem (snapshot)
//                BillItem billItem = new BillItem();
//                billItem.setItemId(orderItem.getItemId());
//                billItem.setItemName(orderItem.getItemName());
//                billItem.setItemPrice(orderItem.getItemPrice());
//                billItem.setQuantity(orderItem.getQuantity());
//
//                int itemTotal = orderItem.getItemPrice() * orderItem.getQuantity();
//                billItem.setTotalPrice(itemTotal);
//
//                subtotal += itemTotal;
//                billItems.add(billItem);
//            }
//        }
//
//        // --------------------------------------------------------------------
//        // 3. Calculate Tax & Total Amount
//        // --------------------------------------------------------------------
//        // Example: 10% tax (can be moved to config later)
//        int tax = (int) (subtotal * 0.10);
//        int totalAmount = subtotal + tax;
//
//        // --------------------------------------------------------------------
//        // 4. Create Bill Object
//        // --------------------------------------------------------------------
//        Bill bill = new Bill();
//
//        bill.setBillNumber("BILL-" + System.currentTimeMillis()); // unique bill number
//        bill.setOrderId(orderId);
//
//        // Customer details copied from Order (important for invoice)
//        bill.setCustomerName(order.getCustomerName());
//        bill.setCustomerPhone(order.getCustomerPhone());
//
//        bill.setSubtotal(subtotal);
//        bill.setTax(tax);
//        bill.setTotalAmount(totalAmount);
//
//        bill.setPaymentMethod("CASH");     // default, can be updated later
//        bill.setPaymentStatus("UNPAID");   // initial state
//
//        bill.setBillTime(LocalDateTime.now());
//        bill.setBillItems(billItems);
//
//        // --------------------------------------------------------------------
//        // 5. Save Bill (BillItems auto-saved due to CascadeType.ALL)
//        // --------------------------------------------------------------------
//        return billRepository.save(bill);
//    }
}
