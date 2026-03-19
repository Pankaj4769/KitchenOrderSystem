package com.kos.repository;

import com.kos.model.SubscriptionPlan;
import com.kos.model.SubscriptionPlan.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    Optional<SubscriptionPlan> findByPlanName(PlanType planName);
}
