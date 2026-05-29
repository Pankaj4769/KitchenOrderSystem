package com.kos.service;

import com.kos.dto.AuthUser;
import com.kos.dto.ProfileSettings;
import com.kos.dto.Restaurent;
import com.kos.dto.UpdateContactRequest;
import com.kos.dto.UpdateRestaurantRequest;
import com.kos.repository.RestaurentRepository;
import com.kos.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class ProfileService {

    private static final Logger logger = LogManager.getLogger(ProfileService.class);

    private static final Pattern MOBILE_RE = Pattern.compile("^\\+?[1-9]\\d{9,14}$");
    private static final Pattern EMAIL_RE  = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository users;
    private final RestaurentRepository restaurants;
    private final OtpService otp;

    public ProfileService(UserRepository users, RestaurentRepository restaurants, OtpService otp) {
        this.users       = users;
        this.restaurants = restaurants;
        this.otp         = otp;
    }

    public ProfileSettings getSettings(String username) {
        AuthUser owner = users.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("User not found: " + username));

        ProfileSettings out = new ProfileSettings();
        out.setOwnerName(owner.getName());
        out.setMobile(owner.getMobile());
        out.setEmail(owner.getEmail());

        Integer restId = parseRestaurantId(owner.getRestaurantId());
        if (restId != null) {
            restaurants.findById(restId).ifPresent(r -> {
                out.setRestaurantName(r.getRestaurentName());
                out.setAddress(r.getAddress());
                out.setGstin(r.getGstin());
                out.setFssai(r.getFssai());
                out.setTaxName(r.getTaxName());
                out.setTaxRate(r.getTaxRate());
                out.setTaxInclusion(r.getTaxInclusion());
                out.setServiceCharge(r.getServiceCharge());
                out.setReceiptHeader(r.getReceiptHeader());
                out.setReceiptFooter(r.getReceiptFooter());
                out.setAutoPrintReceipt(r.getAutoPrintReceipt());
                out.setShowGstinOnReceipt(r.getShowGstinOnReceipt());
            });
        }
        return out;
    }

    @Transactional
    public void updateContact(String username, UpdateContactRequest req) {
        if (req == null || req.getField() == null || req.getNewValue() == null) {
            throw new IllegalArgumentException("field and newValue are required");
        }
        AuthUser owner = users.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("User not found: " + username));

        String field    = req.getField().toLowerCase();
        String newValue = req.getNewValue().trim();

        if (!"mobile".equals(field) && !"email".equals(field)) {
            throw new IllegalArgumentException("field must be 'mobile' or 'email'");
        }

        if ("mobile".equals(field) && !MOBILE_RE.matcher(newValue).matches()) {
            throw new IllegalArgumentException("Invalid mobile format");
        }
        if ("email".equals(field) && !EMAIL_RE.matcher(newValue).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        String currentPrimary   = "mobile".equals(field) ? owner.getMobile() : owner.getEmail();
        String currentAlternate = "mobile".equals(field) ? owner.getEmail()  : owner.getMobile();

        boolean oldVerified =
            (currentPrimary   != null && otp.wasRecentlyVerified(username, currentPrimary)) ||
            (currentAlternate != null && otp.wasRecentlyVerified(username, currentAlternate));
        if (!oldVerified) {
            throw new VerificationRequiredException("Old channel verification required");
        }

        if (!otp.wasRecentlyVerified(username, newValue)) {
            throw new VerificationRequiredException("New value verification required");
        }

        if ("mobile".equals(field)) {
            owner.setMobile(newValue);
        } else {
            owner.setEmail(newValue);
        }
        users.save(owner);

        if (currentPrimary   != null) otp.clearVerification(username, currentPrimary);
        if (currentAlternate != null) otp.clearVerification(username, currentAlternate);
        otp.clearVerification(username, newValue);
    }

    @Transactional
    public ProfileSettings updateRestaurant(String username, UpdateRestaurantRequest req) {
        AuthUser owner = users.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        Integer restId = parseRestaurantId(owner.getRestaurantId());
        if (restId == null) {
            throw new IllegalStateException("User has no restaurantId");
        }
        Restaurent r = restaurants.findById(restId)
            .orElseThrow(() -> new IllegalStateException("Restaurant not found: " + restId));

        if (req.getAddress() != null) {
            r.setAddress(req.getAddress().trim());
        }
        if (req.getGstin() != null && isBlank(r.getGstin())) {
            r.setGstin(req.getGstin().trim().toUpperCase());
        }
        if (req.getFssai() != null && isBlank(r.getFssai())) {
            r.setFssai(req.getFssai().trim());
        }

        // ── Billing & Receipt (always overwritable) ──
        if (req.getTaxName() != null)            r.setTaxName(req.getTaxName().trim());
        if (req.getTaxRate() != null)            r.setTaxRate(req.getTaxRate());
        if (req.getTaxInclusion() != null)       r.setTaxInclusion(normalizeInclusion(req.getTaxInclusion()));
        if (req.getServiceCharge() != null)      r.setServiceCharge(req.getServiceCharge());
        if (req.getReceiptHeader() != null)      r.setReceiptHeader(req.getReceiptHeader());
        if (req.getReceiptFooter() != null)      r.setReceiptFooter(req.getReceiptFooter());
        if (req.getAutoPrintReceipt() != null)   r.setAutoPrintReceipt(req.getAutoPrintReceipt());
        if (req.getShowGstinOnReceipt() != null) r.setShowGstinOnReceipt(req.getShowGstinOnReceipt());

        restaurants.save(r);

        return getSettings(username);
    }

    private static String normalizeInclusion(String s) {
        String v = s.trim().toLowerCase();
        return ("inclusive".equals(v) || "exclusive".equals(v)) ? v : "exclusive";
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static Integer parseRestaurantId(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    public static class VerificationRequiredException extends RuntimeException {
        public VerificationRequiredException(String msg) { super(msg); }
    }
}
