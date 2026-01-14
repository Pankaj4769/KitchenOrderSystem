package com.kos.service;

import com.kos.dto.Bill;

public interface BillingService {

    Bill generateBill(Integer orderId);
}
