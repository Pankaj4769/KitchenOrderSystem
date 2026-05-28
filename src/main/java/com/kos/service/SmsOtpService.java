package com.kos.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class SmsOtpService {

    private static final Logger logger = LogManager.getLogger(SmsOtpService.class);

    // TODO: integrate Twilio / MSG91 / Fast2SMS
    public boolean sendOtp(String mobile, String otp) {
        if (mobile == null || mobile.isBlank()) {
            return false;
        }
        logger.info("SMS OTP for {} -> {} (dev stub)", mobile, otp);
        return true;
    }
}
