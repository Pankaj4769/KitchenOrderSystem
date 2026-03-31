package com.kos.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kos.dto.AuthUser;
import com.kos.dto.OnboardingStatus;
import com.kos.dto.PaymentRequest;
import com.kos.dto.PaymentResponse;
import com.kos.dto.Restaurent;
import com.kos.dto.SubscriptionPlan;
import com.kos.repository.RestaurentRepository;
import com.kos.repository.UserRepository;

@Service
public class PaymentService {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	RestaurentRepository restaurentRepository;

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
			user.setOnboardingStatus(OnboardingStatus.COMPLETED);
			try {
				AuthUser savedUser = userRepository.save(user);
				if(savedUser.getSubscriptionPlan().equals(SubscriptionPlan.valueOf(paymentReq.getPlan()))) {
				
				paymentResponse.setActivePlan(savedUser.getSubscriptionPlan().toString());
				paymentResponse.setPaymentStatus(true);
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
	
	
}
