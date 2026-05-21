package com.kos.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kos.dto.AuthUser;
import com.kos.dto.CartItem;
import com.kos.dto.Item;
import com.kos.dto.MessageResponse;
import com.kos.dto.OnboardingStatus;
import com.kos.dto.Order;
import com.kos.dto.OrderItem;
import com.kos.dto.PaymentData;
import com.kos.dto.PaymentRequest;
import com.kos.dto.PaymentResponse;
import com.kos.dto.Restaurent;
import com.kos.dto.SubscriptionPlan;
import com.kos.repository.CartItemRepository;
import com.kos.repository.InventoryRepository;
import com.kos.repository.OrderRepository;
import com.kos.repository.PaymentDataRepository;
import com.kos.repository.RestaurentRepository;
import com.kos.repository.UserRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Service
public class PaymentService {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	RestaurentRepository restaurentRepository;
	
	@Autowired
	PaymentDataRepository paymentDataRepository;
	
	@Autowired
	InventoryRepository inventoryRepository;
	
	@Autowired
	OrderRepository orderRepo;

	@Autowired
	SubscriptionService subscriptionService;

	public PaymentResponse doPayment(PaymentRequest paymentReq) {
		PaymentResponse paymentResponse = new PaymentResponse();
		
		Optional<AuthUser> dbUser = userRepository.findByMobile(paymentReq.getPhone());
		System.out.println("[doPayment] phone=" + paymentReq.getPhone() + " email=" + paymentReq.getEmail() + " plan=" + paymentReq.getPlan() + " restaurant=" + paymentReq.getRestaurantName());
		System.out.println("[doPayment] userFoundByMobile=" + dbUser.isPresent());
		if (!dbUser.isPresent() && paymentReq.getEmail() != null) {
			dbUser = userRepository.findByEmail(paymentReq.getEmail());
			System.out.println("[doPayment] userFoundByEmail=" + dbUser.isPresent());
		}
		Optional<Restaurent> rest = restaurentRepository.findByRestaurentName(paymentReq.getRestaurantName());
		System.out.println("[doPayment] restaurantFound=" + rest.isPresent());
		if (!rest.isPresent() && paymentReq.getRestaurantName() != null) {
			Restaurent newRest = new Restaurent();
			newRest.setRestaurentName(paymentReq.getRestaurantName());
			rest = Optional.of(restaurentRepository.save(newRest));
			System.out.println("[doPayment] restaurantCreated id=" + rest.get().getRestaurentId());
		}
		if(dbUser.isPresent() && rest.isPresent()) {
			AuthUser user = dbUser.get();
			user.setEmail(paymentReq.getEmail());
			user.setMobile(paymentReq.getPhone());
			user.setFirstTime(false);
			user.setName(paymentReq.getName());
			user.setSubscriptionPlan(SubscriptionPlan.valueOf(paymentReq.getPlan()));
			user.setRestaurantId(rest.get().getRestaurentId().toString());
			// Preserve SETUP_COMPLETE for trial-recovery payments; only
			// new payments should set onboardingStatus to COMPLETED.
			if (user.getOnboardingStatus() != OnboardingStatus.SETUP_COMPLETE) {
				user.setOnboardingStatus(OnboardingStatus.COMPLETED);
			}
			try {
				AuthUser savedUser = userRepository.save(user);
				if(savedUser.getSubscriptionPlan().equals(SubscriptionPlan.valueOf(paymentReq.getPlan()))) {

				// Payment succeeded → activate the subscription row so status
				// flips to ACTIVE with a fresh expiry based on the chosen plan.
				try {
					Long restId = Long.parseLong(savedUser.getRestaurantId());
					subscriptionService.activateSubscriptionAfterPayment(restId, paymentReq.getPlan());
				} catch (NumberFormatException ignored) {
					// non-numeric restaurant id — skip activation, payment still succeeds
				}

				paymentResponse.setActivePlan(savedUser.getSubscriptionPlan().toString());
				paymentResponse.setPaymentStatus(true);
				paymentResponse.setRestaurantId(savedUser.getRestaurantId());
				return paymentResponse;
			}
			}catch(Exception e) {
				throw e;
			}
			
			
		}
		paymentResponse.setActivePlan(null);
		paymentResponse.setPaymentStatus(false);
		return paymentResponse;
		
	}
	
	
	/**
     * Process payment atomically.
     * @param paymentData the payment payload
     * @return saved PaymentData
	 * @throws Exception 
     * @throws PaymentProcessingException if validation fails or save fails
     */
	@Transactional(rollbackFor = Exception.class)
	public MessageResponse processPayment(@Valid @NotNull PaymentData paymentData) {
	    MessageResponse response = new MessageResponse("failure", false);
	    try {
	        // 1️⃣ Validate required fields
	        if (!validatePaymentData(paymentData)) {
	            return response;
	        }

	        // 2️⃣ Set timestamp if missing
	        if (paymentData.getTimestamp() == null) {
	            paymentData.setTimestamp(Instant.now());
	        }

	        // 3️⃣ Handle CartItems safely
	        List<CartItem> managedItems = new ArrayList<>();
	        for (CartItem item : paymentData.getItems()) {
	            if (item.getId() != null) {
	                // Fetch the managed entity from DB
	            	CartItem managedItem  = new CartItem();
	            	managedItem.setAddedToCartStatus(item.getAddedToCartStatus());
	            	managedItem.setQty(item.getQty());
	                managedItem.setNotes(item.getNotes());
	                managedItem.setPortion(item.getPortion());
	                managedItem.setPrice(item.getPrice());
	                managedItem.setCategory(item.getCategory());
	                managedItem.setName(item.getName());
	                managedItem.setImage(item.getImage());
	                managedItems.add(managedItem);
	            } else {
	                // New item, persist as is
	                managedItems.add(item);
	            }
	        }
	        paymentData.setItems(managedItems);

	        // 4️⃣ Save PaymentData (cascade should handle CartItems)
	        PaymentData savedPayment = paymentDataRepository.save(paymentData);

	        if (savedPayment.getId() != null && saveOrder(paymentData)) {
	            response.setMessage("success");
	            response.setStatus(true);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return response;
	}

    private boolean saveOrder(@Valid @NotNull PaymentData paymentData) {
	
    	Order ord = new Order();
    	ord.setCustomerName("Guest");
    	ord.setEstimatedTime(null);
    	List<OrderItem> ordList = new ArrayList<OrderItem>();
    	ord.setKotRound(null);
    	ord.setNotes(null);
    	ord.setOrderNumber("ORD-"+System.currentTimeMillis());
    	ord.setOrderTime(LocalDateTime.now());
    	ord.setPaymentDate(LocalDateTime.now());
    	ord.setPaymentStatus("PAID");
    	ord.setPrepTime(null);
    	ord.setPriority("MEDIUM");
    	ord.setRestaurantId(paymentData.getRestaurantId());
    	ord.setSessionId(null);
    	ord.setStatus("PAID");
    	ord.setTableId(null);
    	ord.setTableName(null);
    	for(CartItem i: paymentData.getItems()) {
    		OrderItem itm = new OrderItem();
    		itm.setId(null);
    		itm.setName(i.getName());
    		itm.setNotes(i.getNotes());
    		itm.setOrder(ord);
    		BigDecimal bd = new BigDecimal(i.getPrice().toString());
    		itm.setPrice(bd.doubleValue());
    		itm.setQuantity(i.getQty());
    		itm.setCategory(i.getCategory());
    		ordList.add(itm);
    		
    	}
    	ord.setItems(ordList);
    	
    	Order saved = orderRepo.save(ord);
    	if(saved.getId() > 0) {
    		return true;
    	}
    	
		return false;
	}


	/**
     * Validate essential fields of payment
     * @throws Exception 
     */
    private boolean validatePaymentData(PaymentData paymentData){
        if (paymentData.getAmount() < 0) {
            return false;
        }

        if (paymentData.getMethod() == null) {
        	return false;
        }

        if (paymentData.getMode() == null) {
        	return false;
        }

        if (!StringUtils.hasText(paymentData.getRestaurantId())) {
        	return false;
        }

        if (paymentData.getItems() == null || paymentData.getItems().isEmpty()) {
        	return false;
        }

        // Optional: Validate partPayments if present
        if (paymentData.getPartPayments() != null) {
            for (var part : paymentData.getPartPayments()) {
                if (part.getAmount() < 0) {
                	return false;
                }
                if (part.getMethod() == null) {
                	return false;
                }
            }
        }
        return true;
    }

    /**
     * Fetch payment by transactionId
     * @throws Exception 
     */
    @Transactional(readOnly = true)
    public PaymentData getPaymentByTransactionId(String transactionId) throws Exception {
        return paymentDataRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new Exception("Payment not found: " + transactionId));
    }

    /**
     * Fetch all payments for a restaurant
     */
    @Transactional(readOnly = true)
    public List<PaymentData> getPaymentsByRestaurant(String restaurantId) {
        return paymentDataRepository.findByRestaurantId(restaurantId);
    }
	
	
	
}
